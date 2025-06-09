from flask import Flask, jsonify, request

app = Flask(__name__)

# Base de datos simulada para el restaurante
menu = [
    {"id": 1, "nombre": "Pizza Margarita", "precio": 150},
    {"id": 2, "nombre": "Hamburguesa Clásica", "precio": 120},
    {"id": 3, "nombre": "Ensalada César", "precio": 100}
]

pedidos = []

# Endpoint para obtener el menú
@app.route('/menu', methods=['GET'])
def obtener_menu():
    return jsonify(menu)

# Endpoint para obtener un platillo por ID
@app.route('/menu/<int:id>', methods=['GET'])
def obtener_platillo(id):
    platillo = next((p for p in menu if p["id"] == id), None)
    if platillo:
        return jsonify(platillo)
    return jsonify({"error": "Platillo no encontrado"}), 404

# Endpoint para agregar un pedido
@app.route('/pedidos', methods=['POST'])
def agregar_pedido():
    nuevo_pedido = request.get_json()
    pedidos.append(nuevo_pedido)
    return jsonify({"mensaje": "Pedido agregado", "pedido": nuevo_pedido}), 201

# Endpoint para obtener todos los pedidos
@app.route('/pedidos', methods=['GET'])
def obtener_pedidos():
    return jsonify(pedidos)

# Endpoint para eliminar un pedido por ID
@app.route('/pedidos/<int:id>', methods=['DELETE'])
def eliminar_pedido(id):
    global pedidos
    pedidos = [p for p in pedidos if p["id"] != id]
    return jsonify({"mensaje": "Pedido eliminado"})

if __name__ == '__main__':
    app.run(debug=True)