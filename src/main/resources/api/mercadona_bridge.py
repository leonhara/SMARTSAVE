#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import logging
import traceback
import os
from flask import Flask, request, jsonify

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger('mercadona_bridge')

# Verificamos si mercapy esta instalado
try:
    import mercapy
    logger.info("Mercapy importado correctamente")
except ImportError as e:
    error_msg = "mercapy no está instalado o no puede ser importado"
    logger.error(f"{error_msg}: {str(e)}")
    sys.exit(1)

# Inicializamos el servidor Web
app = Flask(__name__)

def search_products(query, postcode, limit=20):
    try:
        logger.info(f"Iniciando búsqueda de: '{query}' en código postal {postcode}")
        merca = mercapy.Mercadona(postcode)
        products = merca.search(query)
        logger.info(f"Encontrados {len(products)} productos para '{query}'")

        results = []
        for product in products[:limit]:
            if product.not_found():
                continue

            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except Exception as e:
                unit_price = 0.0

            category_name = 'Sin categoría'
            try:
                if hasattr(product, 'category') and product.category:
                    if isinstance(product.category, list) and len(product.category) > 0:
                        category_name = product.category[0].name if hasattr(product.category[0], 'name') else 'Sin categoría'
                    elif hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
            except Exception as e:
                category_name = 'Sin categoría'

            product_name = product.name or ''
            if product_name:
                try:
                    product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
                except Exception as e:
                    product_name = 'Producto'

            product_id = 'unknown'
            try:
                product_id = str(product.id)
            except Exception as e:
                pass

            try:
                product_data = {
                    'id': product_id,
                    'name': product_name,
                    'brand': product.brand or 'Mercadona',
                    'category': category_name,
                    'unit_price': unit_price,
                    'bulk_price': float(product.bulk_price) if hasattr(product, 'bulk_price') and product.bulk_price else None,
                    'is_discounted': bool(product.is_discounted) if hasattr(product, 'is_discounted') and product.is_discounted is not None else False,
                    'previous_price': float(product.previous_price) if hasattr(product, 'previous_price') and product.previous_price else None,
                    'iva': int(product.iva) if hasattr(product, 'iva') and product.iva else 21,
                    'is_new': bool(product.is_new) if hasattr(product, 'is_new') and product.is_new is not None else False,
                    'is_pack': bool(product.is_pack) if hasattr(product, 'is_pack') and product.is_pack is not None else False,
                    'weight': float(product.weight) if hasattr(product, 'weight') and product.weight else None,
                    'description': product.description if hasattr(product, 'description') and product.description else '',
                    'origin': product.origin if hasattr(product, 'origin') and product.origin else '',
                    'supplier': product.supplier if hasattr(product, 'supplier') and product.supplier else ''
                }
                results.append(product_data)
            except Exception as e:
                pass

        logger.info(f"Procesados {len(results)} productos válidos")
        return {"success": True, "data": results}
    except Exception as e:
        error_details = traceback.format_exc()
        return {"success": False, "error": str(e), "details": error_details}

def get_product_detail(product_id, postcode):
    try:
        warehouse = get_warehouse_from_postcode(postcode)
        product = mercapy.Product(product_id, warehouse=warehouse)

        if product.not_found():
            return {"success": False, "error": "Producto no encontrado"}

        unit_price = 0.0
        try:
            if hasattr(product, 'unit_price') and product.unit_price is not None:
                unit_price = float(product.unit_price)
        except Exception:
            unit_price = 0.0

        category_name = 'Sin categoría'
        try:
            if hasattr(product, 'category') and product.category:
                if hasattr(product.category, 'name') and product.category.name:
                    category_name = product.category.name
        except Exception:
            category_name = 'Sin categoría'

        product_name = product.name or ''
        if product_name:
            try:
                product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
            except Exception:
                product_name = 'Producto'

        try:
            result = {
                'id': str(product.id),
                'name': product_name,
                'brand': product.brand or 'Mercadona',
                'category': category_name,
                'unit_price': unit_price,
                'bulk_price': float(product.bulk_price) if hasattr(product, 'bulk_price') and product.bulk_price else None,
                'is_discounted': bool(product.is_discounted) if hasattr(product, 'is_discounted') and product.is_discounted is not None else False,
                'previous_price': float(product.previous_price) if hasattr(product, 'previous_price') and product.previous_price else None,
                'iva': int(product.iva) if hasattr(product, 'iva') and product.iva else 21,
                'is_new': bool(product.is_new) if hasattr(product, 'is_new') and product.is_new is not None else False,
                'is_pack': bool(product.is_pack) if hasattr(product, 'is_pack') and product.is_pack is not None else False,
                'pack_size': int(product.pack_size) if hasattr(product, 'pack_size') and product.pack_size else None,
                'total_units': int(product.total_units) if hasattr(product, 'total_units') and product.total_units else None,
                'weight': float(product.weight) if hasattr(product, 'weight') and product.weight else None,
                'description': product.description if hasattr(product, 'description') and product.description else '',
                'legal_name': product.legal_name if hasattr(product, 'legal_name') and product.legal_name else '',
                'origin': product.origin if hasattr(product, 'origin') and product.origin else '',
                'supplier': product.supplier if hasattr(product, 'supplier') and product.supplier else '',
                'ean': product.ean if hasattr(product, 'ean') and product.ean else '',
                'slug': product.slug if hasattr(product, 'slug') and product.slug else '',
                'age_check': bool(product.age_check) if hasattr(product, 'age_check') and product.age_check is not None else False,
                'minimum_amount': int(product.minimum_amount) if hasattr(product, 'minimum_amount') and product.minimum_amount else 1
            }
            return {"success": True, "data": result}
        except Exception as e:
            return {"success": False, "error": f"Error procesando producto: {str(e)}"}
    except Exception as e:
        return {"success": False, "error": str(e), "details": traceback.format_exc()}

def get_new_arrivals(postcode, limit=20):
    try:
        merca = mercapy.Mercadona(postcode)
        products = merca.get_new_arrivals()

        results = []
        for product in products[:limit]:
            if product.not_found():
                continue

            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except Exception:
                unit_price = 0.0

            category_name = 'Sin categoría'
            try:
                if hasattr(product, 'category') and product.category:
                    if hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
            except Exception:
                category_name = 'Sin categoría'

            product_name = product.name or ''
            if product_name:
                try:
                    product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
                except Exception:
                    product_name = 'Producto'

            product_id = 'unknown'
            try:
                product_id = str(product.id)
            except Exception:
                pass

            try:
                results.append({
                    'id': product_id,
                    'name': product_name,
                    'brand': product.brand or 'Mercadona',
                    'category': category_name,
                    'unit_price': unit_price,
                    'is_new': True
                })
            except Exception:
                pass
        return {"success": True, "data": results}
    except Exception as e:
        return {"success": False, "error": str(e), "details": traceback.format_exc()}

def get_warehouse_from_postcode(postcode):
    try:
        merca = mercapy.Mercadona(postcode)
        return merca.warehouse
    except Exception:
        return "mad1"

# === RUTAS DE LA API WEB (FLASK) ===

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "OK"}), 200

@app.route('/search', methods=['GET'])
def api_search():
    query = request.args.get('q', '')
    postcode = request.args.get('postcode', '14010')
    limit = int(request.args.get('limit', 25))

    if not query:
        return jsonify({"success": False, "error": "Query es requerido para búsqueda"}), 400

    result = search_products(query, postcode, limit)
    return jsonify(result)

@app.route('/new', methods=['GET'])
def api_new():
    postcode = request.args.get('postcode', '14010')
    limit = int(request.args.get('limit', 30))

    result = get_new_arrivals(postcode, limit)
    return jsonify(result)

@app.route('/detail', methods=['GET'])
def api_detail():
    product_id = request.args.get('id', '')
    postcode = request.args.get('postcode', '14010')

    if not product_id:
        return jsonify({"success": False, "error": "ID del producto es requerido"}), 400

    result = get_product_detail(product_id, postcode)
    return jsonify(result)

if __name__ == '__main__':
    logger.info("Iniciando servidor REST de Mercadona Bridge en el puerto 5000...")
    app.run(host='127.0.0.1', port=5000, debug=False, use_reloader=False)