import re, os

base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
path = os.path.join(base, "src", "main", "resources", "mysql-data.sql")

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

def replace_dateadd(m):
    unit = m.group(1).upper()
    offset = m.group(2).strip()
    base_expr = m.group(3).strip()
    if offset.startswith("-"):
        return f"DATE_SUB({base_expr}, INTERVAL {offset[1:]} {unit})"
    else:
        return f"DATE_ADD({base_expr}, INTERVAL {offset} {unit})"

content = re.sub(
    r"DATEADD\s*\(\s*'(\w+)'\s*,\s*(-?[^,]+?)\s*,\s*([^)]+?)\s*\)",
    replace_dateadd,
    content
)

content = re.sub(
    r"FORMATDATETIME\s*\(([^,]+),\s*'yyyy-MM'\)",
    r"DATE_FORMAT(\1, '%Y-%m')",
    content
)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

remaining = [fn for fn in ["DATEADD", "FORMATDATETIME"] if fn in content]
print("Remaining H2 funcs:", remaining if remaining else "None")
