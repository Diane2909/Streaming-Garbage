from pathlib import Path
import json
import pandas as pd


data_path = "../data"


def load_json_folder():

    folder = Path(f"{data_path}/output")

    data = []

    if not folder.exists():
        return pd.DataFrame()


    for file in folder.glob("*.json"):

        try:
            with open(file, "r") as f:
                # Lecture ligne par ligne
                for line in f:

                    line = line.strip()

                    if not line:
                        continue

                    data.append(
                        json.loads(line)
                    )

        except Exception as e:
            print(
                f"Impossible de lire {file}: {e}"
            )

    df = pd.DataFrame(data)
    df["modificationTime"] = pd.to_datetime(df["modificationTime"])
    df["processTime"] = pd.to_datetime(df["processTime"])
    return df 


def get_input_image_count():

    folder = Path(f"{data_path}/input")

    return len(list(folder.rglob("*.jpg")))
