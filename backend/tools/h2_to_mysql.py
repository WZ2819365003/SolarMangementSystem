#!/usr/bin/env python3
"""Convert H2-specific data.sql to MySQL-compatible mysql-data.sql"""
import re, os

base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
src = os.path.join(base, "src", "main", "resources", "data.sql")
dst = os.path.join(base, "src", "main", "resources", "mysql-data.sql")

with open(src, "r", encoding="utf-8") as f:
    content = f.read()

# 1. CAST(... AS INT) -> CAST(... AS SIGNED)
content = re.sub(r"AS\s+INT\)", "AS SIGNED)", content)

# 2. CAST(... AS VARCHAR) -> CAST(... AS CHAR)
content = re.sub(r"AS\s+VARCHAR\)", "AS CHAR)", content)

# 3. Replace || string concat with CONCAT()
#    Strategy: iteratively replace innermost A || B pairs
def replace_double_pipe(text):
    max_iter = 200
    for _ in range(max_iter):
        # Find pattern: non-|| expr  ||  non-|| expr
        # We look for the simplest pair and wrap in CONCAT
        m = re.search(
            r"""((?:'[^']*'|[A-Za-z_][A-Za-z0-9_.()' ,*/+\-]*?\)|[A-Za-z_][A-Za-z0-9_.]*|CONCAT\([^)]*\)))\s*\|\|\s*((?:'[^']*'|[A-Za-z_][A-Za-z0-9_.()' ,*/+\-]*?\)|[A-Za-z_][A-Za-z0-9_.]*|CONCAT\([^)]*\)))""",
            text
        )
        if not m:
            break
        left = m.group(1).strip()
        right = m.group(2).strip()
        replacement = f"CONCAT({left}, {right})"
        text = text[:m.start()] + replacement + text[m.end():]
    return text

content = replace_double_pipe(content)

# 4. SYSTEM_RANGE(a, b) AS alias -> recursive CTE
def add_recursive_ctes(stmt):
    ranges = re.findall(r"CROSS\s+JOIN\s+SYSTEM_RANGE\((\d+),\s*(\d+)\)\s+AS\s+(\w+)", stmt)
    if not ranges:
        return stmt
    cte_parts = []
    for start, end, alias in ranges:
        cte_parts.append(
            f"  {alias} AS (SELECT {start} AS X UNION ALL SELECT X + 1 FROM {alias} WHERE X < {end})"
        )
    cte = "WITH RECURSIVE\n" + ",\n".join(cte_parts) + "\n"
    for start, end, alias in ranges:
        stmt = re.sub(
            rf"CROSS\s+JOIN\s+SYSTEM_RANGE\({start},\s*{end}\)\s+AS\s+{alias}",
            f"CROSS JOIN {alias}",
            stmt,
        )
    stmt = re.sub(r"(SELECT\s)", cte + r"\1", stmt, count=1)
    return stmt

parts = re.split(r"(?=INSERT\s+INTO)", content)
result_parts = []
for part in parts:
    if "SYSTEM_RANGE" in part:
        part = add_recursive_ctes(part)
    result_parts.append(part)
content = "".join(result_parts)

with open(dst, "w", encoding="utf-8") as f:
    f.write(content)

# Verify
issues = []
for i, line in enumerate(content.split("\n"), 1):
    if line.strip().startswith("--"):
        continue
    if "||" in line:
        issues.append(f"  Line {i}: remaining ||")
    if re.search(r"AS\s+INT\)", line):
        issues.append(f"  Line {i}: AS INT)")
    if re.search(r"AS\s+VARCHAR\)", line):
        issues.append(f"  Line {i}: AS VARCHAR)")
if "SYSTEM_RANGE" in content:
    issues.append("  SYSTEM_RANGE still present")

if issues:
    print("WARNINGS:")
    for issue in issues[:20]:
        print(issue)
else:
    print("No remaining H2-specific syntax detected")
print(f"Written to {dst}")
