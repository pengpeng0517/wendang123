import requests
import json

# 测试新增出库单
def test_add_outbound_order():
    url = "http://localhost:8080/api/outbound/order/add"
    headers = {"Content-Type": "application/json"}
    data = {
        "type": "生产领料",
        "recipientName": "测试用户",
        "remark": "测试出库",
        "details": [
            {
                "materialId": 1,
                "materialCode": "MAT000001",
                "materialName": "123皮料",
                "spec": "",
                "unit": "m",
                "quantity": 2,
                "price": 2,
                "amount": 4
            }
        ]
    }
    response = requests.post(url, headers=headers, data=json.dumps(data))
    print("新增出库单响应:", response.json())
    return response.json()

# 测试审批出库单
def test_approve_outbound_order(order_id):
    url = f"http://localhost:8080/api/outbound/order/approve/{order_id}"
    response = requests.put(url)
    print("审批出库单响应:", response.json())
    return response.json()

# 测试获取库存信息
def test_get_inventory():
    url = "http://localhost:8080/api/inventory/list"
    response = requests.get(url)
    print("库存信息:", response.json())
    return response.json()

if __name__ == "__main__":
    print("=== 测试出库审批功能 ===")
    
    # 1. 先查看当前库存
    print("\n1. 当前库存状态:")
    test_get_inventory()
    
    # 2. 新增出库单
    print("\n2. 新增出库单:")
    add_response = test_add_outbound_order()
    order_id = add_response.get("data", {}).get("id")
    
    if order_id:
        # 3. 审批出库单
        print(f"\n3. 审批出库单 (ID: {order_id}):")
        test_approve_outbound_order(order_id)
        
        # 4. 查看库存是否减少
        print("\n4. 审批后库存状态:")
        test_get_inventory()
    else:
        print("创建出库单失败")