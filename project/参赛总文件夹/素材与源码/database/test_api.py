import urllib.request
import json

r = urllib.request.urlopen('http://localhost:8080/api/user/1/detail')
data = json.loads(r.read().decode('utf-8'))
user = data['data']['user']
print(f"nickname={user['nickname']}")
print(f"username={user['username']}")

# 也检查持仓
holds = data['data']['stockHolds']
for h in holds[:2]:
    print(f"  stock: {h['stockName']}")
