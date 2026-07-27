import pandas as pd

def compute_kpis(df):
    if df.empty:
        return {
            "total": 0,
            "accuracy": 0,
            "confidence": 0,
            "delay": 0,
        }

    return {
        "total": len(df),
        "accuracy": df["is_correct"].mean(),
        "confidence": df["confidence"].mean(),
        "delay": df["delay_seconds"].mean(),
    }