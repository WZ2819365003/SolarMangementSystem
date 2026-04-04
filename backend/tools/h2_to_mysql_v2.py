#!/usr/bin/env python3
"""Convert H2-specific data.sql to MySQL-compatible mysql-data.sql (v2)

Handles:
  - SYSTEM_RANGE(a,b) AS alias  -> WITH RECURSIVE CTE
  - CAST(x AS INT)              -> CAST(x AS SIGNED)
  - CAST(x AS VARCHAR)          -> CAST(x AS CHAR)
  - expr1 || expr2              -> CONCAT(expr1, expr2)  (token-aware)
"""
import os, re, sys

base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
src = os.path.join(base, "src", "main", "resources", "data.sql")
dst = os.path.join(base, "src", "main", "resources", "mysql-data.sql")

with open(src, "r", encoding="utf-8") as f:
    content = f.read()


# ---------- tokeniser for || replacement ----------

def tokenize(sql):
    """Yield (type, value) tokens: 'str', 'op', 'paren', 'other'."""
    i = 0
    n = len(sql)
    while i < n:
        c = sql[i]
        if c == "'" :
            j = i + 1
            while j < n:
                if sql[j] == "'" and j + 1 < n and sql[j+1] == "'":
                    j += 2
                elif sql[j] == "'":
                    j += 1
                    break
                else:
                    j += 1
            yield ("str", sql[i:j])
            i = j
        elif c == '|' and i + 1 < n and sql[i+1] == '|':
            yield ("op", "||")
            i += 2
        elif c in ('(', ')'):
            yield ("paren", c)
            i += 1
        elif c in (' ', '\t', '\n', '\r'):
            j = i
            while j < n and sql[j] in (' ', '\t', '\n', '\r'):
                j += 1
            yield ("ws", sql[i:j])
            i = j
        else:
            j = i
            while j < n and sql[j] not in ("'", '|', '(', ')', ' ', '\t', '\n', '\r'):
                if sql[j] == '|' and j + 1 < n and sql[j+1] == '|':
                    break
                j += 1
            yield ("other", sql[i:j])
            i = j


def replace_pipes(sql):
    """Replace all || concat operators with CONCAT() calls."""
    tokens = list(tokenize(sql))

    changed = True
    while changed:
        changed = False
        # Find the first || token
        for idx, (tp, val) in enumerate(tokens):
            if tp == "op" and val == "||":
                # Gather the left expression (walk backwards)
                left_tokens = collect_expr_left(tokens, idx)
                # Gather the right expression (walk forwards)
                right_tokens = collect_expr_right(tokens, idx)

                left_start = idx - len(left_tokens)
                right_end = idx + 1 + len(right_tokens)

                left_sql = "".join(v for _, v in left_tokens).strip()
                right_sql = "".join(v for _, v in right_tokens).strip()

                replacement = [("other", f"CONCAT({left_sql}, {right_sql})")]
                tokens[left_start:right_end] = replacement
                changed = True
                break

    return "".join(v for _, v in tokens)


def collect_expr_left(tokens, pipe_idx):
    """Collect tokens forming the left operand of || at pipe_idx."""
    result = []
    i = pipe_idx - 1
    # skip whitespace
    while i >= 0 and tokens[i][0] == "ws":
        result.insert(0, tokens[i])
        i -= 1
    if i < 0:
        return result

    depth = 0
    while i >= 0:
        tp, val = tokens[i]
        if tp == "paren" and val == ")":
            depth += 1
            result.insert(0, tokens[i])
            i -= 1
        elif tp == "paren" and val == "(":
            if depth > 0:
                depth -= 1
                result.insert(0, tokens[i])
                i -= 1
            else:
                break
        elif depth > 0:
            result.insert(0, tokens[i])
            i -= 1
        elif tp == "str":
            result.insert(0, tokens[i])
            break
        elif tp == "other":
            result.insert(0, tokens[i])
            # Check if previous token is a function name or keyword
            break
        elif tp == "op":
            break
        elif tp == "ws":
            # peek further left
            if i - 1 >= 0 and tokens[i-1][0] in ("other", "paren"):
                result.insert(0, tokens[i])
                i -= 1
            else:
                break
        else:
            break

    # If the collected expr starts with whitespace and is preceded by a function call + parens,
    # include the function name too
    # Check if result[0] is ')' -> need to look at what's before the matching '('
    # Actually, let's check if we stopped at an 'other' token that looks like a function name
    # followed by '(' — include that too
    first_non_ws = 0
    while first_non_ws < len(result) and result[first_non_ws][0] == "ws":
        first_non_ws += 1

    if first_non_ws < len(result):
        ftp, fval = result[first_non_ws]
        if ftp == "paren" and fval == "(":
            # Check token before our collected range for function name
            check_i = pipe_idx - len(result) - 1
            while check_i >= 0 and tokens[check_i][0] == "ws":
                result.insert(0, tokens[check_i])
                check_i -= 1
            if check_i >= 0 and tokens[check_i][0] == "other":
                result.insert(0, tokens[check_i])

    # strip leading whitespace tokens
    while result and result[0][0] == "ws":
        result.pop(0)

    return result


def collect_expr_right(tokens, pipe_idx):
    """Collect tokens forming the right operand of || at pipe_idx."""
    result = []
    i = pipe_idx + 1
    n = len(tokens)
    # skip whitespace
    while i < n and tokens[i][0] == "ws":
        result.append(tokens[i])
        i += 1
    if i >= n:
        return result

    depth = 0
    while i < n:
        tp, val = tokens[i]
        if tp == "paren" and val == "(":
            depth += 1
            result.append(tokens[i])
            i += 1
        elif tp == "paren" and val == ")":
            if depth > 0:
                depth -= 1
                result.append(tokens[i])
                i += 1
            else:
                break
        elif depth > 0:
            result.append(tokens[i])
            i += 1
        elif tp == "str":
            result.append(tokens[i])
            # Check next non-ws token
            j = i + 1
            while j < n and tokens[j][0] == "ws":
                j += 1
            # If next is || we stop here
            break
        elif tp == "other":
            result.append(tokens[i])
            # Check if next token is '(' => function call, include it
            if i + 1 < n and tokens[i+1][0] == "paren" and tokens[i+1][1] == "(":
                i += 1
                continue
            break
        elif tp == "op":
            break
        elif tp == "ws":
            if i + 1 < n and tokens[i+1][0] not in ("op",):
                result.append(tokens[i])
                i += 1
            else:
                break
        else:
            break

    # strip trailing whitespace
    while result and result[-1][0] == "ws":
        result.pop()

    return result


# ---------- main transforms ----------

# 1) || -> CONCAT()
content = replace_pipes(content)

# 2) CAST(x AS INT) -> CAST(x AS SIGNED)   (multiline aware)
content = re.sub(r"\bAS\s+INT\b", "AS SIGNED", content)

# 3) CAST(x AS VARCHAR) -> CAST(x AS CHAR)
content = re.sub(r"\bAS\s+VARCHAR\b", "AS CHAR", content)

# 4) SYSTEM_RANGE -> WITH RECURSIVE CTE
def add_recursive_ctes(stmt):
    ranges = re.findall(
        r"CROSS\s+JOIN\s+SYSTEM_RANGE\((\d+),\s*(\d+)\)\s+AS\s+(\w+)", stmt
    )
    if not ranges:
        return stmt
    ctes = []
    for s, e, alias in ranges:
        ctes.append(
            f"  {alias} AS (SELECT {s} AS X UNION ALL SELECT X + 1 FROM {alias} WHERE X < {e})"
        )
    cte_block = "WITH RECURSIVE\n" + ",\n".join(ctes) + "\n"
    for s, e, alias in ranges:
        stmt = re.sub(
            rf"CROSS\s+JOIN\s+SYSTEM_RANGE\({s},\s*{e}\)\s+AS\s+{alias}",
            f"CROSS JOIN {alias}",
            stmt,
        )
    stmt = re.sub(r"(SELECT\s)", cte_block + r"\1", stmt, count=1)
    return stmt

parts = re.split(r"(?=INSERT\s+INTO)", content)
content = "".join(add_recursive_ctes(p) if "SYSTEM_RANGE" in p else p for p in parts)

# ---------- write ----------
with open(dst, "w", encoding="utf-8") as f:
    f.write(content)

# ---------- verify ----------
issues = []
for i, line in enumerate(content.split("\n"), 1):
    stripped = line.strip()
    if stripped.startswith("--"):
        continue
    if "||" in line:
        issues.append(f"  Line {i}: remaining ||  ->  {stripped[:80]}")
    if re.search(r"\bAS\s+INT\b", line):
        issues.append(f"  Line {i}: AS INT")
    if re.search(r"\bAS\s+VARCHAR\b", line):
        issues.append(f"  Line {i}: AS VARCHAR")
if "SYSTEM_RANGE" in content:
    issues.append("  SYSTEM_RANGE still present")

if issues:
    print(f"WARNINGS ({len(issues)}):")
    for w in issues[:30]:
        print(w)
else:
    print("All H2 syntax converted successfully")
print(f"Written to {dst}")
