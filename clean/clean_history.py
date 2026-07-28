from pathlib import Path
import shutil

folders = [
    "../data/input",
    "../data/output",
    "../data/checkpoint"
]

for folder in folders:
    path = Path(folder)

    # Remove folder and everything inside
    if path.exists():
        shutil.rmtree(path)

    # Recreate empty folder
    path.mkdir(parents=True, exist_ok=True)