import os
from datetime import datetime


# ===================== 初始化与辅助函数 =====================
def init_todo_file():
    """初始化待办文件（不存在则创建，添加异常处理）"""
    try:
        if not os.path.exists("todo_list.txt"):
            with open("todo_list.txt", "w", encoding="utf-8") as f:
                f.write("")
            print("待办文件初始化成功！")
    except PermissionError:
        print("错误：无文件写入权限，请检查目录权限设置！")
        exit(1)
    except Exception as e:
        print(f"文件初始化失败：{str(e)}")
        exit(1)


def read_todo_data():
    """读取待办数据（过滤空行和格式错误项，返回结构化列表）"""
    init_todo_file()
    try:
        with open("todo_list.txt", "r", encoding="utf-8") as f:
            lines = [line.strip() for line in f.readlines() if line.strip()]
        todo_data = []
        for line in lines:
            parts = line.split("|", 2)
            if len(parts) == 3 and parts[0] in ["0", "1"]:
                todo_data.append({
                    "status": parts[0],
                    "task": parts[1],
                    "deadline": parts[2]
                })
            else:
                print(f"警告：忽略格式错误的记录：{line}")
        return todo_data
    except Exception as e:
        print(f"读取待办数据失败：{str(e)}")
        return []


def write_todo_data(todo_data):
    """写入待办数据到文件"""
    try:
        with open("todo_list.txt", "w", encoding="utf-8") as f:
            for item in todo_data:
                f.write(f"{item['status']}|{item['task']}|{item['deadline']}\n")
        return True
    except Exception as e:
        print(f"写入待办数据失败：{str(e)}")
        return False


def check_duplicate(task_name):
    """检查事项是否重复（不区分大小写）"""
    todo_data = read_todo_data()
    for item in todo_data:
        if task_name.lower() == item["task"].lower():
            return True
    return False


def check_expired_tasks():
    """启动时检查过期未完成事项并提醒"""
    todo_data = read_todo_data()
    expired_tasks = []
    today = datetime.now().date()

    for item in todo_data:
        if item["status"] == "0" and item["deadline"] != "无":
            try:
                deadline_date = datetime.strptime(item["deadline"], "%Y-%m-%d").date()
                if deadline_date < today:
                    expired_tasks.append(item)
            except:
                continue

    if expired_tasks:
        print("\n⚠️  过期提醒：以下未完成事项已超过截止时间！")
        for idx, item in enumerate(expired_tasks, 1):
            print(f"{idx}. {item['task']}（截止时间：{item['deadline']}）")
        print()


# ===================== 核心功能函数 =====================
def add_todo():
    """添加待办事项（支持重复检查、日期校验）"""
    print("\n===== 添加待办事项 =====")
    task_name = input("请输入待办事项名称：").strip()
    if not task_name:
        print("❌ 错误：事项名称不能为空！")
        return

    # 重复检查
    if check_duplicate(task_name):
        confirm = input(f"⚠️  警告：已存在相同/相似事项，是否继续添加？(y/n)：").strip().lower()
        if confirm != "y":
            print("已取消添加！")
            return

    # 日期校验与过期提醒
    deadline = input("请输入截止时间（格式：YYYY-MM-DD，留空则为无）：").strip()
    if deadline:
        try:
            deadline_date = datetime.strptime(deadline, "%Y-%m-%d")
            if deadline_date < datetime.now():
                confirm = input(f"⚠️  警告：截止时间已过期，是否继续添加？(y/n)：").strip().lower()
                if confirm != "y":
                    print("已取消添加！")
                    return
        except ValueError:
            print("❌ 日期格式错误！将不记录截止时间。")
            deadline = "无"
    else:
        deadline = "无"

    # 写入数据
    todo_data = read_todo_data()
    todo_data.append({
        "status": "0",
        "task": task_name,
        "deadline": deadline
    })

    if write_todo_data(todo_data):
        print("✅ 待办事项添加成功！")
    else:
        print("❌ 待办事项添加失败！")


def view_todos():
    """查看待办事项（支持筛选、排序、统计）"""
    todo_data = read_todo_data()
    if not todo_data:
        print("\n📭 当前暂无待办事项！")
        return

    print("\n===== 我的待办清单 =====")
    # 统计信息
    total = len(todo_data)
    completed = sum(1 for item in todo_data if item["status"] == "1")
    pending = total - completed
    print(f"📊 统计：总计{total}项 | 已完成{completed}项 | 未完成{pending}项")

    # 筛选功能
    filter_choice = input("\n是否需要筛选？(1-未完成 / 2-已完成 / 3-全部，默认3)：").strip()
    if filter_choice == "1":
        filtered_data = [item for item in todo_data if item["status"] == "0"]
        print(f"\n🔍 筛选结果：未完成事项（共{pending}项）")
    elif filter_choice == "2":
        filtered_data = [item for item in todo_data if item["status"] == "1"]
        print(f"\n🔍 筛选结果：已完成事项（共{completed}项）")
    else:
        filtered_data = todo_data
        print(f"\n🔍 筛选结果：全部事项（共{total}项）")

    if not filtered_data:
        print("📭 该筛选条件下无匹配事项！")
        return

    # 排序功能
    sort_choice = input("排序方式？(1-添加顺序 / 2-截止时间，默认1)：").strip()
    if sort_choice == "2":
        # 按截止时间排序（无截止时间放最后）
        filtered_data.sort(key=lambda x:
        datetime.strptime(x["deadline"], "%Y-%m-%d") if x["deadline"] != "无" else datetime.max
                           )

    # 显示结果
    for idx, item in enumerate(filtered_data, 1):
        status_text = "✅ 已完成" if item["status"] == "1" else "❌ 未完成"
        print(f"\n{idx}. {status_text}")
        print(f"   事项：{item['task']}")
        print(f"   截止时间：{item['deadline']}")
    print("\n=======================")


def search_todo():
    """搜索待办事项（支持关键词匹配）"""
    print("\n===== 搜索待办事项 =====")
    keyword = input("请输入搜索关键词：").strip().lower()
    if not keyword:
        print("❌ 错误：关键词不能为空！")
        return

    todo_data = read_todo_data()
    results = [
        item for item in todo_data
        if keyword in item["task"].lower() or keyword in item["deadline"].lower()
    ]

    if not results:
        print(f"📭 未找到包含'{keyword}'的待办事项！")
        return

    print(f"\n🔍 找到{len(results)}项匹配结果：")
    for idx, item in enumerate(results, 1):
        status_text = "✅ 已完成" if item["status"] == "1" else "❌ 未完成"
        print(f"\n{idx}. {status_text}")
        print(f"   事项：{item['task']}")
        print(f"   截止时间：{item['deadline']}")
    print("\n=======================")


def mark_completed():
    """标记待办事项为已完成"""
    view_todos()
    todo_data = read_todo_data()
    if not todo_data:
        return

    try:
        idx = int(input("\n请输入要标记完成的待办序号：").strip())
        if idx < 1 or idx > len(todo_data):
            print("❌ 错误：序号不存在！")
            return

        todo_data[idx - 1]["status"] = "1"
        if write_todo_data(todo_data):
            print("✅ 标记完成成功！")
        else:
            print("❌ 标记完成失败！")
    except ValueError:
        print("❌ 错误：请输入有效的数字序号！")


def edit_todo():
    """编辑待办事项（支持修改名称和截止时间）"""
    view_todos()
    todo_data = read_todo_data()
    if not todo_data:
        return

    try:
        idx = int(input("\n请输入要编辑的待办序号：").strip())
        if idx < 1 or idx > len(todo_data):
            print("❌ 错误：序号不存在！")
            return

        target_item = todo_data[idx - 1]
        print(f"\n当前事项：")
        print(f"   事项：{target_item['task']}")
        print(f"   截止时间：{target_item['deadline']}")

        # 编辑选项
        print("\n编辑选项：")
        print("1. 修改事项名称")
        print("2. 修改截止时间")
        print("3. 同时修改两者")
        edit_choice = input("请选择编辑类型（1-3）：").strip()

        new_task = target_item["task"]
        new_deadline = target_item["deadline"]

        if edit_choice in ["1", "3"]:
            new_task = input("请输入新的事项名称（留空则保持不变）：").strip() or target_item["task"]
            # 重复检查
            if check_duplicate(new_task) and new_task != target_item["task"]:
                confirm = input(f"⚠️  警告：已存在相同/相似事项，是否继续修改？(y/n)：").strip().lower()
                if confirm != "y":
                    print("已取消修改！")
                    return

        if edit_choice in ["2", "3"]:
            new_deadline = input("请输入新的截止时间（格式：YYYY-MM-DD，留空则为无）：").strip()
            if new_deadline:
                try:
                    datetime.strptime(new_deadline, "%Y-%m-%d")
                    if datetime.strptime(new_deadline, "%Y-%m-%d") < datetime.now():
                        confirm = input(f"⚠️  警告：截止时间已过期，是否继续？(y/n)：").strip().lower()
                        if confirm != "y":
                            print("已取消修改！")
                            return
                except ValueError:
                    print("❌ 日期格式错误！将保持原截止时间。")
                    new_deadline = target_item["deadline"]
            else:
                new_deadline = "无"

        # 更新数据
        todo_data[idx - 1]["task"] = new_task
        todo_data[idx - 1]["deadline"] = new_deadline

        if write_todo_data(todo_data):
            print("✅ 待办事项修改成功！")
        else:
            print("❌ 待办事项修改失败！")

    except ValueError:
        print("❌ 错误：请输入有效的数字序号！")


def delete_todo():
    """删除待办事项（支持二次确认）"""
    view_todos()
    todo_data = read_todo_data()
    if not todo_data:
        return

    try:
        idx = int(input("\n请输入要删除的待办序号：").strip())
        if idx < 1 or idx > len(todo_data):
            print("❌ 错误：序号不存在！")
            return

        confirm = input(f"确认要删除「{todo_data[idx - 1]['task']}」吗？(y/n)：").strip().lower()
        if confirm != "y":
            print("已取消删除！")
            return

        del todo_data[idx - 1]
        if write_todo_data(todo_data):
            print("✅ 删除成功！")
        else:
            print("❌ 删除失败！")
    except ValueError:
        print("❌ 错误：请输入有效的数字序号！")


def batch_operation():
    """批量操作（批量标记完成/批量删除）"""
    print("\n===== 批量操作 =====")
    print("1. 批量标记已完成")
    print("2. 批量删除待办")
    choice = input("请选择操作类型（1-2）：").strip()
    if choice not in ["1", "2"]:
        print("❌ 错误：无效的选择！")
        return

    todo_data = read_todo_data()
    if not todo_data:
        print("📭 当前暂无待办事项！")
        return

    view_todos()
    print("\n请输入要操作的序号（多个序号用逗号分隔，如：1,3,5）：")
    input_str = input().strip()
    if not input_str:
        print("❌ 错误：未输入任何序号！")
        return

    # 解析序号
    try:
        indices = [int(x.strip()) for x in input_str.split(",") if x.strip().isdigit()]
        valid_indices = [i for i in indices if 1 <= i <= len(todo_data)]
        invalid_indices = [i for i in indices if i not in valid_indices]

        if invalid_indices:
            print(f"⚠️  警告：以下序号无效，将忽略：{','.join(map(str, invalid_indices))}")
        if not valid_indices:
            print("❌ 错误：无有效序号可操作！")
            return

        # 执行操作
        confirm = input(f"确认要对{len(valid_indices)}个事项执行操作？(y/n)：").strip().lower()
        if confirm != "y":
            print("已取消操作！")
            return

        if choice == "1":
            # 批量标记完成
            for idx in valid_indices:
                todo_data[idx - 1]["status"] = "1"
            if write_todo_data(todo_data):
                print(f"✅ 成功标记{len(valid_indices)}个事项为已完成！")
        else:
            # 批量删除（倒序删除避免索引错乱）
            for idx in sorted(valid_indices, reverse=True):
                del todo_data[idx - 1]
            if write_todo_data(todo_data):
                print(f"✅ 成功删除{len(valid_indices)}个事项！")

    except Exception as e:
        print(f"❌ 操作失败：{str(e)}")


# ===================== 主程序入口 =====================
def main():
    init_todo_file()
    print("=" * 40)
    print("🎉 欢迎使用个人待办清单工具（优化版）🎉")
    print("=" * 40)

    # 启动时检查过期事项
    check_expired_tasks()

    while True:
        print("\n📋 功能菜单：")
        print("1. 添加待办事项 (快捷键：a)")
        print("2. 查看/筛选待办 (快捷键：v)")
        print("3. 搜索待办事项 (快捷键：s)")
        print("4. 标记待办为已完成 (快捷键：m)")
        print("5. 编辑待办事项 (快捷键：e)")
        print("6. 删除待办事项 (快捷键：d)")
        print("7. 批量操作 (快捷键：b)")
        print("8. 退出工具 (快捷键：q)")

        choice = input("\n请输入功能序号或快捷键：").strip().lower()
        print("-" * 40)

        # 快捷键映射
        key_map = {
            "a": "1", "v": "2", "s": "3", "m": "4",
            "e": "5", "d": "6", "b": "7", "q": "8"
        }
        if choice in key_map:
            choice = key_map[choice]

        # 功能分发
        if choice == "1":
            add_todo()
        elif choice == "2":
            view_todos()
        elif choice == "3":
            search_todo()
        elif choice == "4":
            mark_completed()
        elif choice == "5":
            edit_todo()
        elif choice == "6":
            delete_todo()
        elif choice == "7":
            batch_operation()
        elif choice == "8":
            confirm = input("确认要退出吗？(y/n)：").strip().lower()
            if confirm == "y":
                print("👋 感谢使用，再见！")
                break
            else:
                print("已取消退出，返回菜单～")
        else:
            print("❌ 输入错误，请选择1-8或对应的快捷键！")

        if choice != "8":
            input("\n按回车键返回菜单...")
            print("\n" * 2)  # 清空屏幕效果


if __name__ == "__main__":
    main()