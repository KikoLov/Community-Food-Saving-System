import argparse
import csv
import json
import os
import sys
import urllib.parse
import urllib.request


def fetch_curve(base_url, token, strategy, original_price, min_price, total_hours, category_factor=None):
    params = {
        "originalPrice": original_price,
        "minPrice": min_price,
        "totalHours": total_hours,
        "strategy": strategy,
    }
    if category_factor is not None:
        params["categoryFactor"] = category_factor
    url = f"{base_url.rstrip('/')}/api/admin/pricing/curve?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url)
    req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req, timeout=20) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        if data.get("code") != 200:
            raise RuntimeError(f"API error for {strategy}: {data}")
        return data["data"]["points"]


def save_csv(path, rows):
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["hour", "exponential", "tiered", "linear"])
        for row in rows:
            writer.writerow(row)


def save_plot(path, rows):
    try:
        import matplotlib.pyplot as plt
    except Exception as e:
        raise RuntimeError(
            "matplotlib 未安装，无法生成 PNG。请先执行: pip install matplotlib\n"
            f"原始错误: {e}"
        )

    x = [r[0] for r in rows]
    y_exp = [r[1] for r in rows]
    y_tier = [r[2] for r in rows]
    y_lin = [r[3] for r in rows]

    plt.figure(figsize=(10, 5))
    plt.plot(x, y_exp, label="Exponential", linewidth=2)
    plt.plot(x, y_tier, label="Tiered", linewidth=2)
    plt.plot(x, y_lin, label="Linear", linewidth=2)
    plt.title("Dynamic Pricing Curve Comparison")
    plt.xlabel("Elapsed Hour")
    plt.ylabel("Price")
    plt.grid(alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(path, dpi=160)
    plt.close()


def main():
    parser = argparse.ArgumentParser(description="Generate dynamic pricing curve chart for paper.")
    parser.add_argument("--base-url", default="http://localhost:8080", help="Backend base URL")
    parser.add_argument("--token", required=True, help="Admin JWT token")
    parser.add_argument("--original-price", type=float, default=20.0)
    parser.add_argument("--min-price", type=float, default=6.0)
    parser.add_argument("--total-hours", type=int, default=72)
    parser.add_argument("--category-factor", type=float, default=None)
    parser.add_argument("--out-dir", default="docs/figures")
    args = parser.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)

    exp_points = fetch_curve(
        args.base_url, args.token, "exponential",
        args.original_price, args.min_price, args.total_hours, args.category_factor
    )
    tier_points = fetch_curve(
        args.base_url, args.token, "tiered",
        args.original_price, args.min_price, args.total_hours, args.category_factor
    )
    lin_points = fetch_curve(
        args.base_url, args.token, "linear",
        args.original_price, args.min_price, args.total_hours, args.category_factor
    )

    rows = []
    for i in range(len(exp_points)):
        hour = exp_points[i]["hour"]
        rows.append([
            hour,
            float(exp_points[i]["price"]),
            float(tier_points[i]["price"]),
            float(lin_points[i]["price"]),
        ])

    csv_path = os.path.join(args.out_dir, "pricing_curve_comparison.csv")
    png_path = os.path.join(args.out_dir, "pricing_curve_comparison.png")
    save_csv(csv_path, rows)
    save_plot(png_path, rows)

    print(f"CSV generated: {csv_path}")
    print(f"PNG generated: {png_path}")


if __name__ == "__main__":
    try:
        main()
    except Exception as ex:
        print(f"[ERROR] {ex}", file=sys.stderr)
        sys.exit(1)
