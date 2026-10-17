import pymysql

conn = pymysql.connect(
    host='localhost',
    port=3306,
    user='root',
    password='123456',
    database='finance_db',
    charset='utf8mb4',
    cursorclass=pymysql.cursors.DictCursor
)
cursor = conn.cursor()

# 清空旧数据（TRUNCATE 重置自增ID）
cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
cursor.execute("TRUNCATE TABLE user_stock_hold")
cursor.execute("TRUNCATE TABLE user_asset")
cursor.execute("TRUNCATE TABLE user")
cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

# 插入用户
users = [
    ('user1', '123456', '理财新手'),
    ('user2', '123456', '基金达人'),
    ('user3', '123456', '债券玩家'),
    ('user4', '123456', 'ETF投资者'),
    ('user5', '123456', '价值投资派'),
    ('user6', '123456', '分散配置者'),
    ('user7', '123456', '激进交易者'),
    ('user8', '123456', '稳健理财师'),
    ('user9', '123456', '指数定投者'),
    ('user10', '123456', '金融学习者'),
]
cursor.executemany("INSERT INTO user (username, password, nickname) VALUES (%s, %s, %s)", users)

# 插入资产配置
assets = [
    (1, 20, 30, 20, 15, 0, 15),
    (2, 10, 60, 10, 15, 0, 5),
    (3, 5, 10, 70, 5, 0, 10),
    (4, 0, 20, 10, 60, 0, 10),
    (5, 40, 20, 20, 10, 5, 5),
    (6, 25, 25, 25, 20, 3, 2),
    (7, 50, 10, 5, 20, 10, 5),
    (8, 15, 25, 35, 15, 0, 10),
    (9, 0, 30, 10, 50, 0, 10),
    (10, 30, 20, 20, 20, 5, 5),
]
cursor.executemany("INSERT INTO user_asset (user_id, stock_ratio, fund_ratio, bond_ratio, etf_ratio, derivatives_ratio, cash_ratio) VALUES (%s, %s, %s, %s, %s, %s, %s)", assets)

# 插入持仓
holds = [
    (1, '600519', '贵州茅台', 100, 1800.00),
    (1, '000858', '五粮液', 200, 160.00),
    (2, '601318', '中国平安', 500, 45.00),
    (2, '600036', '招商银行', 300, 35.00),
    (3, '600519', '贵州茅台', 50, 1750.00),
    (4, '510300', '沪深300ETF', 1000, 3.80),
    (4, '159915', '创业板ETF', 500, 1.90),
    (5, '600519', '贵州茅台', 200, 1700.00),
    (5, '601888', '中国中免', 150, 70.00),
    (6, '600519', '贵州茅台', 80, 1850.00),
    (6, '000858', '五粮液', 300, 165.00),
    (6, '601318', '中国平安', 400, 46.00),
    (7, '600519', '贵州茅台', 300, 1600.00),
    (7, '000858', '五粮液', 800, 150.00),
    (7, '601318', '中国平安', 1000, 42.00),
    (8, '600036', '招商银行', 600, 34.00),
    (8, '601318', '中国平安', 200, 47.00),
    (9, '510300', '沪深300ETF', 2000, 3.50),
    (9, '159919', '沪深300ETF易方达', 1500, 4.20),
    (10, '600519', '贵州茅台', 150, 1750.00),
    (10, '601318', '中国平安', 350, 44.00),
    (10, '600036', '招商银行', 250, 36.00),
]
cursor.executemany("INSERT INTO user_stock_hold (user_id, stock_code, stock_name, hold_num, buy_price) VALUES (%s, %s, %s, %s, %s)", holds)

conn.commit()

# 验证
cursor.execute("SELECT id, username, nickname FROM user LIMIT 3")
for row in cursor.fetchall():
    print(f"id={row['id']}, username={row['username']}, nickname={row['nickname']}")

print("\nDone! Data inserted successfully.")
cursor.close()
conn.close()
