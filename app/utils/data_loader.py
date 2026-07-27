from pathlib import Path
import json
import pandas as pd
import streamlit as st

data_path = "../data"

@st.cache_data(ttl=3)
def load_json_folder():

    folder = Path(f"{data_path}/output")

    rows = []

    if not folder.exists():
        return pd.DataFrame()

    for file in folder.rglob("*.json"):
        try:
            with open(file, "r") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    rows.append(json.loads(line))
        except Exception as e:
            print(
                f"Impossible de lire {file}: {e}"
            )

    df = pd.DataFrame(rows)

    if not df.empty:
        df["modificationTime"] = pd.to_datetime(df["modificationTime"])
        df["processTime"] = pd.to_datetime(df["processTime"])

        df["delay"] = df["processTime"] - df["modificationTime"]
        df["delay_seconds"] = df["delay"].dt.total_seconds()
        df["delay_ms"] = df["delay_seconds"] * 1000
        
        df["size_kb"] = df["length"] / 1024

    return df

def get_input_image_count():

    folder = Path(f"{data_path}/input")

    return len(list(folder.rglob("*.jpg")))
