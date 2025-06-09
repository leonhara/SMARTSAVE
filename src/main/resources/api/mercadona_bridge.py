#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import argparse
import logging
import traceback
import os

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger('mercadona_bridge')

#Vamos a verificar si mercapy esta instalado
try:
    import mercapy
    logger.info("Mercapy importado correctamente")
except ImportError as e:
    error_msg = "mercapy no está instalado o no puede ser importado"
    logger.error(f"{error_msg}: {str(e)}")
    print(json.dumps({"success": False, "error": error_msg}))
    sys.exit(1)

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

            #Obtenemos el valor del precio de forma segura
            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except Exception as e:
                logger.warning(f"Error al procesar unit_price: {str(e)}")
                unit_price = 0.0

            #Aqui obtenemos el nombre de la categoría de forma segura
            category_name = 'Sin categoría'
            #Aqui estan los manejos de errores de category_name
            try:
                if hasattr(product, 'category') and product.category:
                    if isinstance(product.category, list) and len(product.category) > 0:
                        category_name = product.category[0].name if hasattr(product.category[0], 'name') else 'Sin categoría'
                    elif hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
                    else:
                        category_name = 'Sin categoría'
                else:
                    category_name = 'Sin categoría'
            except Exception as e:
                logger.warning(f"Error al procesar category_name: {str(e)}")
                category_name = 'Sin categoría'

            #Aqui limpiaremos el nombre del producto para evitar problemas
            product_name = product.name or ''
            if product_name:
                try:
                    product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
                except Exception as e:
                    logger.warning(f"Error al procesar product_name: {str(e)}")
                    product_name = 'Producto'

            #Cogeremos el ID
            product_id = 'unknown'
            try:
                product_id = str(product.id)
            except Exception as e:
                logger.warning(f"Error al procesar product_id: {str(e)}")

            # Reunir todos los datos del producto
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
                logger.warning(f"Error al crear el diccionario del producto: {str(e)}")

        logger.info(f"Procesados {len(results)} productos válidos")
        return {"success": True, "data": results}
    except Exception as e:
        error_details = traceback.format_exc()
        logger.error(f"Error en búsqueda: {str(e)}\n{error_details}")
        return {"success": False, "error": str(e), "details": error_details}

def get_product_detail(product_id, postcode):
    try:
        logger.info(f"Obteniendo detalles del producto ID: {product_id} CP: {postcode}")
        warehouse = get_warehouse_from_postcode(postcode)
        product = mercapy.Product(product_id, warehouse=warehouse)

        if product.not_found():
            logger.warning(f"Producto no encontrado: {product_id}")
            return {"success": False, "error": "Producto no encontrado"}

        #obtendremos los valores de forma segura
        unit_price = 0.0
        try:
            if hasattr(product, 'unit_price') and product.unit_price is not None:
                unit_price = float(product.unit_price)
        except Exception as e:
            logger.warning(f"Error al procesar unit_price: {str(e)}")
            unit_price = 0.0

        category_name = 'Sin categoría'
        try:
            if hasattr(product, 'category') and product.category:
                if hasattr(product.category, 'name') and product.category.name:
                    category_name = product.category.name
        except Exception as e:
            logger.warning(f"Error al procesar category_name: {str(e)}")
            category_name = 'Sin categoría'

        # Aqui se limpia el nombre del producto
        product_name = product.name or ''
        if product_name:
            try:
                product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
            except Exception as e:
                logger.warning(f"Error al procesar product_name: {str(e)}")
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
            logger.info(f"Detalles obtenidos para producto {product_id}")
            return {"success": True, "data": result}
        except Exception as e:
            logger.error(f"Error creando resultado para producto {product_id}: {str(e)}")
            return {"success": False, "error": f"Error procesando producto: {str(e)}"}
    except Exception as e:
        error_details = traceback.format_exc()
        logger.error(f"Error obteniendo detalles del producto {product_id}: {str(e)}\n{error_details}")
        return {"success": False, "error": str(e), "details": error_details}

def get_new_arrivals(postcode, limit=20):
    try:
        logger.info(f"Obteniendo productos nuevos para CP: {postcode}")
        merca = mercapy.Mercadona(postcode)
        products = merca.get_new_arrivals()
        logger.info(f"Encontrados {len(products)} productos nuevos")

        results = []
        for product in products[:limit]:
            if product.not_found():
                continue

            #Volvemos a obtener  valores de forma segura
            unit_price = 0.0
            try:
                if hasattr(product, 'unit_price') and product.unit_price is not None:
                    unit_price = float(product.unit_price)
            except Exception as e:
                logger.warning(f"Error al procesar unit_price: {str(e)}")
                unit_price = 0.0

            category_name = 'Sin categoría'
            try:
                if hasattr(product, 'category') and product.category:
                    if hasattr(product.category, 'name') and product.category.name:
                        category_name = product.category.name
            except Exception as e:
                logger.warning(f"Error al procesar category_name: {str(e)}")
                category_name = 'Sin categoría'

            #volvemos a limpiar nombre del producto
            product_name = product.name or ''
            if product_name:
                try:
                    product_name = product_name.encode('utf-8', errors='ignore').decode('utf-8')
                except Exception as e:
                    logger.warning(f"Error al procesar product_name: {str(e)}")
                    product_name = 'Producto'

            #Cogemos el id seguro
            product_id = 'unknown'
            try:
                product_id = str(product.id)
            except Exception as e:
                logger.warning(f"Error al procesar product_id: {str(e)}")

            try:
                results.append({
                    'id': product_id,
                    'name': product_name,
                    'brand': product.brand or 'Mercadona',
                    'category': category_name,
                    'unit_price': unit_price,
                    'is_new': True
                })
            except Exception as e:
                logger.warning(f"Error al crear el diccionario del producto nuevo: {str(e)}")

        logger.info(f"Procesados {len(results)} productos nuevos válidos")
        return {"success": True, "data": results}
    except Exception as e:
        error_details = traceback.format_exc()
        logger.error(f"Error obteniendo productos nuevos: {str(e)}\n{error_details}")
        return {"success": False, "error": str(e), "details": error_details}

def get_warehouse_from_postcode(postcode):
    try:
        logger.info(f"Obteniendo warehouse para CP: {postcode}")
        merca = mercapy.Mercadona(postcode)
        warehouse = merca.warehouse
        logger.info(f"Warehouse obtenido: {warehouse}")
        return warehouse
    except Exception as e:
        logger.warning(f"Error obteniendo warehouse, usando default. Error: {str(e)}")
        return "mad1"  # Madrid por defecto

def main():
    try:
        logger.info(f"Python version: {sys.version}")
        logger.info(f"Current working directory: {os.getcwd()}")
        logger.info(f"Script directory: {os.path.dirname(os.path.abspath(__file__))}")
        logger.info(f"Environment: {os.environ.get('PYTHONPATH', 'PYTHONPATH not set')}")

        parser = argparse.ArgumentParser(description='Mercadona API Bridge')
        parser.add_argument('action', choices=['search', 'detail', 'new'], help='Acción a realizar')
        parser.add_argument('query', nargs='?', help='Término de búsqueda o ID del producto')
        parser.add_argument('--postcode', default='28001', help='Código postal')
        parser.add_argument('--limit', type=int, default=20, help='Límite de resultados')

        args = parser.parse_args()
        logger.info(f"Ejecutando acción: {args.action} con query: {args.query}, CP: {args.postcode}, límite: {args.limit}")

        if args.action == 'search':
            if not args.query:
                error_msg = "Query es requerido para búsqueda"
                logger.error(error_msg)
                print(json.dumps({"success": False, "error": error_msg}))
                return
            result = search_products(args.query, args.postcode, args.limit)
        elif args.action == 'detail':
            if not args.query:
                error_msg = "ID del producto es requerido"
                logger.error(error_msg)
                print(json.dumps({"success": False, "error": error_msg}))
                return
            result = get_product_detail(args.query, args.postcode)
        elif args.action == 'new':
            result = get_new_arrivals(args.postcode, args.limit)

        #Vamos a verificar que el resultado sea válido
        if not isinstance(result, dict):
            logger.error(f"Resultado inválido, se esperaba un diccionario pero se obtuvo: {type(result)}")
            result = {"success": False, "error": "Resultado de procesamiento inválido"}

        output = json.dumps(result, ensure_ascii=False, indent=2)
        print(output)
    except Exception as e:
        error_details = traceback.format_exc()
        logger.error(f"Error en función main: {str(e)}\n{error_details}")
        print(json.dumps({"success": False, "error": str(e), "details": error_details}, ensure_ascii=False))

if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        error_details = traceback.format_exc()
        logger.critical(f"Error crítico no controlado: {str(e)}\n{error_details}")
        print(json.dumps({"success": False, "error": f"Error crítico: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)
