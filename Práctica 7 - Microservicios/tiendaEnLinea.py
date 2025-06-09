# Ejemplo de microservicios usando Flask (Python)
# Microservicio: Gestión de productos

from flask import Flask, jsonify, request # type: ignore

app = Flask(__name__)

# Simulación de base de datos de productos
productos = [
    {"id": 1, "nombre": "Laptop", "precio": 15000},
    {"id": 2, "nombre": "Teclado mecánico", "precio": 1200},
    {"id": 3, "nombre": "Mouse inalámbrico", "precio": 850}
]

@app.route('/productos', methods=['GET'])
def obtener_productos():
    return jsonify(productos)

@app.route('/productos/<int:id>', methods=['GET'])
def obtener_producto(id):
    producto = next((p for p in productos if p['id'] == id), None)
    if producto:
        return jsonify(producto)
    return jsonify({"error": "Producto no encontrado"}), 404

@app.route('/productos', methods=['POST'])
def agregar_producto():
    nuevo = request.get_json()
    nuevo['id'] = len(productos) + 1
    productos.append(nuevo)
    return jsonify(nuevo), 201

if __name__ == '__main__':
    app.run(port=5000, debug=True)
