#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import argparse

# Verificar si mercapy está instalado
try:
    import mercapy
except ImportError:
    print(json.dumps({"success": False, "error": "mercapy no está instalado"}))
    sys.exit(1)

def search_products(query, postcode, limit=20):
    try:
        merca = mercapy.Mercadona(postcode)
        products = merca.search(query)

        results = []
        for product in products[:limit]:
            if product.not_found():
                continue

            # Obtener el valor del precio de forma segura
            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except:
                unit_price = 0.0

            # Obtener el nombre de la categoría de forma segura
            category_name = 'Sin categoría'
            try:
                if hasattr(product, 'category') and product.category:
                    if hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
            except:
                category_name = 'Sin categoría'

            # Limpiar el nombre del producto para evitar problemas de encoding
            product_name = product.name or ''
            if product_name:
                product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')

            results.append({
                'id': str(product.id),  # Asegurar que el ID sea string
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
            })

        return {"success": True, "data": results}
    except Exception as e:
        return {"success": False, "error": str(e)}

def get_product_detail(product_id, postcode):
    try:
        # Obtener el warehouse por código postal
        warehouse = get_warehouse_from_postcode(postcode)
        product = mercapy.Product(product_id, warehouse=warehouse)

        if product.not_found():
            return {"success": False, "error": "Producto no encontrado"}

        # Obtener valores de forma segura
        unit_price = 0.0
        try:
            if hasattr(product, 'unit_price') and product.unit_price is not None:
                unit_price = float(product.unit_price)
        except:
            unit_price = 0.0

        category_name = 'Sin categoría'
        try:
            if hasattr(product, 'category') and product.category:
                if hasattr(product.category, 'name') and product.category.name:
                    category_name = product.category.name
        except:
            category_name = 'Sin categoría'

        # Limpiar nombre del producto
        product_name = product.name or ''
        if product_name:
            product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')

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
        return {"success": False, "error": str(e)}

def get_new_arrivals(postcode, limit=20):
    try:
        merca = mercapy.Mercadona(postcode)
        products = merca.get_new_arrivals()

        results = []
        for product in products[:limit]:
            if product.not_found():
                continue

            # Obtener valores de forma segura
            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except:
                unit_price = 0.0

            category_name = 'Sin categoría'
            try:
                if hasattr(product, 'category') and product.category:
                    if hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
            except:
                category_name = 'Sin categoría'

            # Limpiar nombre del producto
            product_name = product.name or ''
            if product_name:
                product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')

            results.append({
                'id': str(product.id),
                'name': product_name,
                'brand': product.brand or 'Mercadona',
                'category': category_name,
                'unit_price': unit_price,
                'is_new': True
            })

        return {"success": True, "data": results}
    except Exception as e:
        return {"success": False, "error": str(e)}

def get_warehouse_from_postcode(postcode):
    """Obtiene el warehouse correspondiente al código postal"""
    try:
        merca = mercapy.Mercadona(postcode)
        return merca.warehouse
    except:
        return "mad1"  # Madrid por defecto

def main():
    parser = argparse.ArgumentParser(description='Mercadona API Bridge')
    parser.add_argument('action', choices=['search', 'detail', 'new'], help='Acción a realizar')
    parser.add_argument('query', nargs='?', help='Término de búsqueda o ID del producto')
    parser.add_argument('--postcode', default='28001', help='Código postal')
    parser.add_argument('--limit', type=int, default=20, help='Límite de resultados')

    args = parser.parse_args()

    if args.action == 'search':
        if not args.query:
            print(json.dumps({"success": False, "error": "Query es requerido para búsqueda"}))
            return
        result = search_products(args.query, args.postcode, args.limit)
    elif args.action == 'detail':
        if not args.query:
            print(json.dumps({"success": False, "error": "ID del producto es requerido"}))
            return
        result = get_product_detail(args.query, args.postcode)
    elif args.action == 'new':
        result = get_new_arrivals(args.postcode, args.limit)

    # Usar ensure_ascii=False para manejar caracteres especiales correctamente
    print(json.dumps(result, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()