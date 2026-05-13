import os
import csv

# ====== 1. 指定要检查的文件夹路径 ======
folder_path = r"D:\test_files"   # 目录路径
output_csv = "result.csv"

# 存放合法和非法文件
valid_files = []
invalid_files = []

# ====== 2. 遍历文件夹 ======
for filename in os.listdir(folder_path):
    # 只处理 .py 文件
    if not filename.endswith(".py"):
        invalid_files.append(filename)
        continue

    name_without_ext = filename[:-3]   # 去掉 .py
    parts = name_without_ext.split("-")

    # 判断是否符合 1-学号-姓名 结构
    if len(parts) != 3:
        invalid_files.append(filename)
        continue

    prefix, student_id, name = parts

    # 进一步校验规则
    if prefix != "1":
        invalid_files.append(filename)
        continue

    if not student_id.isdigit():
        invalid_files.append(filename)
        continue

    if name.strip() == "":
        invalid_files.append(filename)
        continue

    # 合法文件
    valid_files.append((student_id, name, filename))

# ====== 3. 写入 CSV 文件 ======
with open(output_csv, "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.writer(f)
    writer.writerow(["序号", "学号", "姓名", "文件名"])

    for index, data in enumerate(valid_files, start=1):
        writer.writerow([index, data[0], data[1], data[2]])

print("CSV 文件已生成：", output_csv)

# ====== 4. 输出不符合要求的文件 ======
if invalid_files:
    print("\n不符合要求的文件名：")
    for name in invalid_files:
        print(name)
else:
    print("\n所有文件名均符合要求 ✅")
