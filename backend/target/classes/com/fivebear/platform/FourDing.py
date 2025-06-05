import json
from itertools import combinations, product

#剩余金额：186898.4 成功锁定金额：30961.0

def read_json_file(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data

def group_key(number, positions):
    # 生成分组key，positions为定的位置
    return ''.join([number[i] if i in positions else 'X' for i in range(len(number))])

class Code:
    def __init__(self, number, amount):
        self.number = number
        self.total_amount = amount
        self.locked_amount = 0

    @property
    def remain_amount(self):
        return round(self.total_amount - self.locked_amount, 8)

    def lock(self, amount):
        if self.remain_amount >= amount:
            self.locked_amount += amount
            return True
        return False

    def unlock(self, amount):
        self.locked_amount -= amount
        if self.locked_amount < 0:
            self.locked_amount = 0
            
    def to_string(self):
        return f'{self.number}: 总金额={self.total_amount}, 已锁定={self.locked_amount}, 剩余={self.remain_amount}'

class CodeManager:
    def __init__(self, code_list):
        self.codes = {c.number: c for c in code_list}
        self.group2dict = self.build_real_2ding_groups()
        self.group3dict = self.build_real_3ding_groups()

    def get_codes_by_key(self, key):
        """
        根据2定/3定分组key（如'12XX'、'1X34'等）返回所有匹配该key的4定号码对象列表。
        """
        positions = [i for i, ch in enumerate(key) if ch != 'X']
        matched = []
        for code in self.codes.values():
            if all(code.number[pos] == key[pos] for pos in positions):
                matched.append(code)
        return matched

    def get_codes_by_group(self, positions, key):
        # 返回该分组下所有剩余金额>0的号码对象
        return [c for c in self.codes.values() if group_key(c.number, positions) == key and c.remain_amount > 0]
    
    def convert_group(self, key, target_count):
        """
        尝试对指定key分组进行转换。
        参数:
            key: 分组key（如'12XX'）
            target_count: 目标号码数（如2定100，3定10）
        返回:
            (success, total_locked_amount, locked_codes)
            - success: 是否转换成功
            - total_locked_amount: 本组锁定总金额
            - locked_codes: 锁定的Code对象列表
        """
        codes = self.get_codes_by_key(key)
        if len(codes) != target_count:
            return False, 0, []
        if any(code.remain_amount < 0.1 for code in codes):
            return False, 0, []
        min_amount = min(code.remain_amount for code in codes)
        for code in codes:
            code.lock(min_amount)
        total_locked_amount = min_amount * len(codes)
        return True, total_locked_amount, codes

    def try_lock_group(self, group_key, target_count, target_amount):
        # 尝试锁定该分组下target_count个号码，累计金额达到target_amount
        # 如果不够，释放已锁定金额
        ...

    def find_and_validate_group(self, key, target_count):
        """
        查找key下所有4定号码对象，并验证组数是否等于target_count。
        返回(是否符合, 匹配号码对象列表)
        """
        codes = self.get_codes_by_key(key)
        is_valid = len(codes) == target_count
        return is_valid, codes

    def all_2ding_3ding_groups(self):
        """
        返回所有2定和3定分组合并的字典：
        {
            "2ding": {key: [Code, ...], ...},
            "3ding": {key: [Code, ...], ...}
        }
        """
        return {
            "2ding": self.build_all_2ding_groups(),
            "3ding": self.build_all_3ding_groups()
        }

    def build_all_2ding_groups(self):
        """
        返回所有2定分组，格式为 {key: [Code, ...], ...}
        """
        N = 4
        pos_combos = list(combinations(range(N), 2))
        group2list = []
        for pos in pos_combos:
            for digits in product(range(10), repeat=2):
                key = ['X'] * N
                key[pos[0]] = str(digits[0])
                key[pos[1]] = str(digits[1])
                group_key_str = ''.join(key)
                group2list.append(Code(group_key_str, 0))
        return group2list

    def build_all_3ding_groups(self):
        """
        返回所有3定分组，格式为 {key: [Code, ...], ...}
        """
        N = 4
        pos_combos = list(combinations(range(N), 3))
        group3list = []
        for pos in pos_combos:
            for digits in product(range(10), repeat=3):
                key = ['X'] * N
                key[pos[0]] = str(digits[0])
                key[pos[1]] = str(digits[1])
                key[pos[2]] = str(digits[2])
                group_key_str = ''.join(key)
                group3list.append(Code(group_key_str, 0))
        return group3list

    def build_real_2ding_groups(self):
        N = 4
        pos_combos = list(combinations(range(N), 2))
        group2dict = {}
        for code in self.codes.values():
            for pos in pos_combos:
                key = ['X'] * N
                key[pos[0]] = code.number[pos[0]]
                key[pos[1]] = code.number[pos[1]]
                group_key_str = ''.join(key)
                group2dict.setdefault(group_key_str, []).append(code)
        return group2dict

    def build_real_3ding_groups(self):
        N = 4
        pos_combos = list(combinations(range(N), 3))
        group3dict = {}
        for code in self.codes.values():
            for pos in pos_combos:
                key = ['X'] * N
                key[pos[0]] = code.number[pos[0]]
                key[pos[1]] = code.number[pos[1]]
                key[pos[2]] = code.number[pos[2]]
                group_key_str = ''.join(key)
                group3dict.setdefault(group_key_str, []).append(code)
        return group3dict

    def remain_total_amount(self):
        return sum(code.remain_amount for code in self.codes.values())

if __name__ == "__main__":
    data = read_json_file('code.json')
    code_list = [Code(item['number'].strip().zfill(4), item['bet_amount']) for item in data]
    manager = CodeManager(code_list)

    group_limits = {'2ding': 100, '3ding': 10}
    group_locked_amount = {'2ding': 0, '3ding': 0}
    success_codes = []

    for group_type, group_dict in [('2ding', manager.group2dict), ('3ding', manager.group3dict)]:
        print(f"\n{group_type}分组详细信息:")
        matched_count = 0
        for key, codes in group_dict.items():
            if len(codes) != group_limits[group_type]:
                continue
            is_valid, total_locked_amount, locked_codes = manager.convert_group(key, group_limits[group_type])
            if is_valid:
                group_locked_amount[group_type] += total_locked_amount
                print(f'分组: {key} 锁定成功，锁定金额：{total_locked_amount}，锁定号码:')
                for code_item in locked_codes:
                    print(code_item.to_string())
                success_codes.append((key, total_locked_amount, locked_codes))
                matched_count += 1
            else:
                pass  # 可选：输出失败信息
        print(f'{group_type} 成功锁定分组数: {matched_count}')

    print('='*100)
    print(f'成功锁定分组总数：{sum(1 for _ in success_codes)}')
    print(f'2定锁定金额：{group_locked_amount["2ding"]}')
    print(f'3定锁定金额：{group_locked_amount["3ding"]}')
    print('='*100)
    print(f'剩余金额：{manager.remain_total_amount()}')

    # 金额守恒校验
    original_total = sum(code.total_amount for code in manager.codes.values())
    remain_total = manager.remain_total_amount()
    converted_total = group_locked_amount['2ding'] + group_locked_amount['3ding'] + remain_total
    print(f'原始4定总金额：{original_total}')
    print(f'2定转换总金额：{group_locked_amount["2ding"]}')
    print(f'3定转换总金额：{group_locked_amount["3ding"]}')
    print(f'剩余4定金额：{remain_total}')
    print(f'转换后总金额：{converted_total}')
    print(f'差值：{converted_total - original_total}')
    if abs(converted_total - original_total) < 1e-6:
        print('校验通过，转换前后金额一致！')
    else:
        print('校验失败，转换前后金额不一致！')
    
