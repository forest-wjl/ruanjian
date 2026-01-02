import os
import time
import json
import random
import datetime
from urllib.parse import quote


class LifeHelper:
    def __init__(self):
        # 初始化本地数据文件
        self.data_path = "life_helper_data.json"
        self.txt_data_path = "life_helper_data.txt"  # TXT可读文件路径
        self._load_data()

    def _load_data(self):
        """加载本地数据（待办、记账）"""
        if os.path.exists(self.data_path):
            with open(self.data_path, "r", encoding="utf-8") as f:
                self.data = json.load(f)
        else:
            self.data = {
                "todos": [],
                "expenses": []
            }

    def _save_data(self):
        """保存数据到本地（同时保存JSON和TXT格式）"""
        # 1. 保存JSON文件（程序运行依赖）
        with open(self.data_path, "w", encoding="utf-8") as f:
            json.dump(self.data, f, ensure_ascii=False, indent=2)

        # 2. 保存TXT文件（纯文本可读格式）
        with open(self.txt_data_path, "w", encoding="utf-8") as f:
            # 写入文件头部
            f.write("=" * 30 + "\n")
            f.write("        本地生活小助手数据\n")
            f.write("=" * 30 + "\n\n")

            # 写入待办事项
            f.write("【待办事项】\n")
            if not self.data["todos"]:
                f.write("暂无待办事项\n")
            else:
                for i, todo in enumerate(self.data["todos"], 1):
                    status = "✅ 已完成" if todo["done"] else "🔲 未完成"
                    f.write(f"{i}. 内容：{todo['content']}\n")
                    f.write(f"   时间：{todo['time']}\n")
                    f.write(f"   状态：{status}\n\n")

            f.write("-" * 25 + "\n\n")

            # 写入记账数据
            f.write("【记账数据】\n")
            if not self.data["expenses"]:
                f.write("暂无支出记录\n")
            else:
                total_amount = 0.0
                for exp in self.data["expenses"]:
                    f.write(f"项目：{exp['item']}\n")
                    f.write(f"金额：{exp['amount']:.2f}元\n")
                    f.write(f"时间：{exp['time']}\n\n")
                    total_amount += exp["amount"]
                f.write(f"所有支出总金额：{total_amount:.2f}元\n")

            # 写入文件尾部
            f.write("\n" + "=" * 30 + "\n")
            f.write(f"最后更新：{datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write("=" * 30 + "\n")

        # 提示TXT文件位置
        txt_abs_path = os.path.abspath(self.txt_data_path)
        print(f"✅ 可读TXT数据文件已保存：{txt_abs_path}")

    # 功能1：待办事项管理（新增自定义时间功能）
    def todo_manager(self):
        print("\n==== 待办事项管理 ====")
        print("1. 添加待办（可自定义时间）")
        print("2. 查看待办")
        print("3. 标记待办为完成")
        print("4. 删除待办事项")
        choice = input("请选择操作（1/2/3/4）：")

        if choice == "1":
            content = input("输入待办内容：")
            # 新增：选择是否自定义时间
            custom_time_choice = input("是否自定义待办时间？（y/n，默认当前日期）：").lower()
            todo_time = ""

            if custom_time_choice == "y":
                # 循环校验，直到输入正确格式的时间
                while True:
                    time_input = input("输入待办时间（格式：YYYY-MM-DD，如：2026-02-15）：")
                    try:
                        # 验证时间格式是否有效
                        datetime.datetime.strptime(time_input, "%Y-%m-%d")
                        todo_time = time_input
                        print(f"✅ 已选择待办时间：{todo_time}")
                        break
                    except ValueError:
                        print("❌ 时间格式无效！请严格按照YYYY-MM-DD格式输入（如2026-01-01），重新输入：")
            else:
                # 使用当前日期作为默认时间
                todo_time = datetime.date.today().strftime("%Y-%m-%d")
                print(f"✅ 使用默认时间：{todo_time}")

            # 添加待办到数据列表
            self.data["todos"].append({
                "content": content,
                "done": False,
                "time": todo_time
            })
            self._save_data()
            print("✅ 待办添加成功！")

        elif choice == "2":
            if not self.data["todos"]:
                print("暂无待办事项~")
                return
            print("\n==== 我的待办列表 ====")
            for i, todo in enumerate(self.data["todos"], 1):
                status = "✅ 已完成" if todo["done"] else "🔲 未完成"
                print(f"序号：{i} | 内容：{todo['content']} | 创建/指定时间：{todo['time']} | 状态：{status}")

        elif choice == "3":
            if not self.data["todos"]:
                print("暂无待办事项~")
                return
            print("\n==== 我的待办列表 ====")
            for i, todo in enumerate(self.data["todos"], 1):
                status = "✅ 已完成" if todo["done"] else "🔲 未完成"
                print(f"序号：{i} | 内容：{todo['content']} | 状态：{status}")
            try:
                idx = int(input("\n输入要标记完成的待办序号：")) - 1
                if 0 <= idx < len(self.data["todos"]):
                    if self.data["todos"][idx]["done"]:
                        print("⚠️  该待办已标记为完成，无需重复操作！")
                    else:
                        self.data["todos"][idx]["done"] = True
                        self._save_data()
                        print("✅ 待办标记为完成！")
                else:
                    print("❌ 序号无效！")
            except ValueError:
                print("❌ 请输入有效的数字序号！")

        elif choice == "4":
            if not self.data["todos"]:
                print("暂无待办事项，无需删除！")
                return
            print("\n==== 我的待办列表 ====")
            for i, todo in enumerate(self.data["todos"], 1):
                status = "✅ 已完成" if todo["done"] else "🔲 未完成"
                print(f"序号：{i} | 内容：{todo['content']} | 状态：{status}")
            try:
                idx = int(input("\n输入要删除的待办序号：")) - 1
                if 0 <= idx < len(self.data["todos"]):
                    todo_content = self.data["todos"][idx]["content"]
                    confirm = input(f"确定要删除待办「{todo_content}」吗？（y/n）：").lower()
                    if confirm == "y":
                        del self.data["todos"][idx]
                        self._save_data()
                        print(f"✅ 已成功删除待办「{todo_content}」！")
                    else:
                        print("⚠️  已取消删除操作！")
                else:
                    print("❌ 序号无效！")
            except ValueError:
                print("❌ 请输入有效的数字序号！")

        else:
            print("❌ 无效操作选项，请选择1-4！")

    # 功能2：简易记账
    def expense_tracker(self):
        print("\n==== 简易记账 ====")
        print("1. 添加支出")
        print("2. 查看本月支出")
        choice = input("请选择操作（1/2）：")

        if choice == "1":
            item = input("输入支出项目：")
            amount = float(input("输入金额："))
            self.data["expenses"].append({
                "item": item,
                "amount": amount,
                "time": datetime.date.today().strftime("%Y-%m-%d")
            })
            self._save_data()
            print("✅ 记账成功！")

        elif choice == "2":
            month = datetime.date.today().strftime("%Y-%m")
            total = 0.0
            print(f"\n==== {month} 支出明细 ====")
            for exp in self.data["expenses"]:
                if exp["time"].startswith(month):
                    print(f"{exp['time']} | {exp['item']} | {exp['amount']:.2f}元")
                    total += exp["amount"]
            print(f"本月总支出：{total:.2f}元")

    # 功能3：随机日程推荐
    def random_schedule(self):
        print("\n==== 随机日程推荐 ====")
        schedules = [
            "今天可以看一部高分电影（推荐：《肖申克的救赎》）",
            "花30分钟做一组居家运动（比如帕梅拉15分钟燃脂操）",
            "读20页书，然后写3句读书笔记",
            "整理手机相册，把重复照片删除",
            "给很久没联系的朋友发一条问候消息"
        ]
        print(f"💡 今日推荐：{random.choice(schedules)}")

    # 功能4：生成搜索链接
    def gen_search_link(self):
        print("\n==== 生成搜索链接 ====")
        keyword = input("输入要搜索的内容：")
        link = f"https://www.baidu.com/s?wd={quote(keyword)}"
        print(f"🔗 搜索链接：{link}")
        print("提示：复制链接到浏览器打开即可搜索")

    # 功能5：安全密码生成器
    def password_generator(self):
        print("\n==== 安全密码生成器 ====")
        lower_chars = "abcdefghijklmnopqrstuvwxyz"
        upper_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        digits = "0123456789"
        symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        try:
            pwd_length = int(input("输入密码长度（建议8位及以上）："))
            if pwd_length < 4:
                print("❌ 密码长度建议不小于4位！")
                return
            use_symbol = input("是否包含特殊字符？（y/n）：").lower() == "y"
        except ValueError:
            print("❌ 输入无效，请输入数字！")
            return

        char_pool = lower_chars + upper_chars + digits
        if use_symbol:
            char_pool += symbols

        pwd_list = []
        pwd_list.append(random.choice(lower_chars))
        pwd_list.append(random.choice(upper_chars))
        pwd_list.append(random.choice(digits))
        if use_symbol:
            pwd_list.append(random.choice(symbols))

        if pwd_length > len(pwd_list):
            pwd_list += random.choices(char_pool, k=pwd_length - len(pwd_list))

        random.shuffle(pwd_list)
        password = "".join(pwd_list)

        print(f"✅ 生成的密码：{password}")
        print("提示：请妥善保存密码，避免泄露！")

    # 功能6：文本内容统计
    def text_statistics(self):
        print("\n==== 文本内容统计 ====")
        file_path = input("输入文本文件路径（如：./note.txt）：")
        if not os.path.isfile(file_path) or not file_path.endswith((".txt", ".md")):
            print("❌ 无效的文本文件（仅支持.txt/.md格式）！")
            return

        try:
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
                lines = content.splitlines()
        except Exception as e:
            print(f"❌ 文件读取失败：{str(e)}")
            return

        total_chars = len(content)
        pure_chars = len(content.replace(" ", "").replace("\n", "").replace("\t", ""))
        line_count = len(lines)
        word_count = len([word for word in content.split() if word.strip()])

        print(f"\n==== 文本统计结果 ====")
        print(f"总字符数（含空格/换行）：{total_chars}")
        print(f"纯字符数（不含空格/换行）：{pure_chars}")
        print(f"文本总行数：{line_count}")
        print(f"单词数（按空格分割）：{word_count}")

    # 功能7：日期计算器
    def date_calculator(self):
        print("\n==== 日期计算器 ====")
        print("1. 计算两个日期的天数差")
        print("2. 推算N天前/后的日期")
        choice = input("请选择操作（1/2）：")

        def parse_date(date_str):
            try:
                return datetime.datetime.strptime(date_str, "%Y-%m-%d").date()
            except ValueError:
                return None

        if choice == "1":
            date1_str = input("输入第一个日期（格式：YYYY-MM-DD）：")
            date2_str = input("输入第二个日期（格式：YYYY-MM-DD）：")
            date1 = parse_date(date1_str)
            date2 = parse_date(date2_str)

            if not date1 or not date2:
                print("❌ 日期格式无效！请按YYYY-MM-DD输入")
                return

            day_diff = abs((date1 - date2).days)
            print(f"✅ 两个日期相差：{day_diff} 天")

        elif choice == "2":
            try:
                days = int(input("输入天数（正数=未来，负数=过去）："))
            except ValueError:
                print("❌ 天数必须是数字！")
                return
            current_date = datetime.date.today()
            target_date = current_date + datetime.timedelta(days=days)
            print(f"✅ 当前日期：{current_date.strftime('%Y-%m-%d')}")
            print(f"✅ {days} 天后/前的日期：{target_date.strftime('%Y-%m-%d')}")

    # 功能8：凯撒密码加密/解密
    def caesar_cipher(self):
        print("\n==== 凯撒密码加密/解密 ====")
        print("1. 加密文本（含中文占位混淆）")
        print("2. 解密文本")
        choice = input("请选择操作（1/2）：")
        text = input("输入要处理的文本：")
        try:
            shift = int(input("输入偏移量（建议1-25）："))
        except ValueError:
            print("❌ 偏移量必须是数字！")
            return

        result = []
        for char in text:
            if char.islower():
                new_char = chr((ord(char) - ord('a') + shift * (1 if choice == "1" else -1)) % 26 + ord('a'))
                result.append(new_char)
            elif char.isupper():
                new_char = chr((ord(char) - ord('A') + shift * (1 if choice == "1" else -1)) % 26 + ord('A'))
                result.append(new_char)
            else:
                result.append(char)

        final_text = "".join(result)

        if choice == "1":
            chinese_placeholders = ["的", "了", "在", "是", "我", "你", "他", "她", "它", "们", "这", "那"]
            mixed_text = []
            for char in final_text:
                mixed_text.append(char)
                if random.random() > 0.5:
                    mixed_text.append(random.choice(chinese_placeholders))
            final_text = "".join(mixed_text)
            print(f"✅ 加密后（含中文占位）：{final_text}")
        else:
            print(f"✅ 解密后文本：{final_text}")

    # 功能9：系统信息查询
    def system_info_query(self):
        print("\n==== 系统信息查询 ====")
        if os.name == "nt":
            sys_type = "Windows 系统"
        elif os.name == "posix":
            sys_type = "Linux/Mac OS 系统"
        else:
            sys_type = "未知系统"

        try:
            if os.name == "nt":
                username = os.getlogin()
            else:
                import pwd
                username = pwd.getpwuid(os.getuid()).pw_name
        except:
            username = "无法获取"

        work_dir = os.getcwd()

        print(f"==== 系统信息汇总 ====")
        print(f"操作系统类型：{sys_type}")
        print(f"当前登录用户名：{username}")
        print(f"当前工作目录：{work_dir}")

    # 主界面
    def run(self):
        while True:
            print("\n" + "=" * 30)
            print("      本地生活小助手")
            print("=" * 30)
            print("1.  待办事项管理（含自定义时间+删除）")
            print("2.  简易记账")
            print("3.  随机日程推荐")
            print("4.  生成搜索链接")
            print("5.  安全密码生成器")
            print("6.  文本内容统计")
            print("7.  日期计算器")
            print("8.  凯撒密码加密/解密")
            print("9.  系统信息查询")
            print("0.  退出程序")
            choice = input("请选择功能（0-9）：")

            if choice == "1":
                self.todo_manager()
            elif choice == "2":
                self.expense_tracker()
            elif choice == "3":
                self.random_schedule()
            elif choice == "4":
                self.gen_search_link()
            elif choice == "5":
                self.password_generator()
            elif choice == "6":
                self.text_statistics()
            elif choice == "7":
                self.date_calculator()
            elif choice == "8":
                self.caesar_cipher()
            elif choice == "9":
                self.system_info_query()
            elif choice == "0":
                print("👋 程序已退出，再见！")
                break
            else:
                print("❌ 无效选择，请输入0-9之间的数字！")
            time.sleep(1)


if __name__ == "__main__":
    helper = LifeHelper()
    # 启动时打印TXT文件路径
    txt_abs_path = os.path.abspath(helper.txt_data_path)
    print(f"📄 可读数据文件将保存到：{txt_abs_path}")
    helper.run()