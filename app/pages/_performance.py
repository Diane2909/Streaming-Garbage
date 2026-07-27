import streamlit as st
import plotly.express as px
from utils.data_loader import load_json_folder
from utils.data_loader import get_input_image_count
from streamlit_autorefresh import st_autorefresh


st_autorefresh(
    interval=3000,
    key="refresh_perf"
)


df = load_json_folder()


st.title(
    "⚡ Performance Pipeline"
)

df["delay"] = (df["processTime"] - df["modificationTime"])

# KPI

c1,c2,c3,c4 = st.columns(4)


c1.metric(
    "Images traitées",
    len(df)
)


c2.metric(
    "Images en attente",
    f"{get_input_image_count() - len(df)}"
)


c3.metric(
    "Temps moyen inference",
    f"{df.inference_time.mean():.2f}s"
)


c4.metric(
    "Latence totale",
    f"{df.total_time.mean():.2f}s"
)



# évolution temporelle

fig = px.line(
    df.sort_values("processTime"),
    x="processTime",
    y="delay_seconds"
)

""" fig = px.line(
    df,
    x="timestamp",
    y=[
        "transfer_time",
        "preprocessing_time",
        "inference_time"
    ],
    title="Evolution des temps"
) """


st.plotly_chart(
    fig,
    use_container_width=True
)