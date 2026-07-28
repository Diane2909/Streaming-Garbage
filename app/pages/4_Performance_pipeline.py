import streamlit as st
import plotly.express as px
from streamlit_autorefresh import st_autorefresh
from utils.data_loader import load_json_folder
from utils.data_loader import get_input_image_count
import pandas as pd
from datetime import datetime, timedelta

st_autorefresh(interval=2000, key="refresh_perf")

df = load_json_folder()

st.title("Performance du pipeline")

p50 = df["delay_ms"].quantile(0.50)
p95 = df["delay_ms"].quantile(0.95)
p99 = df["delay_ms"].quantile(0.99)

now = pd.Timestamp.now(tz="Europe/Paris")

latest = df["processTime"].max()
if latest > now - timedelta(seconds=20):
    throughput = len(df[df["processTime"] > (latest - timedelta(seconds=10))]) / 10
else:
    throughput = 0

c1, c2, c3, c4, c5, c6 = st.columns(6)
c1.metric(
    "Images traitées",
    len(df)
)
c2.metric(
    "Images en attente",
    f"{get_input_image_count() - len(df)}"
)
c3.metric("P50", f"{p50:.2f}ms")
c4.metric("P95", f"{p95:.2f}ms")
c5.metric("P99", f"{p99:.2f}ms")
c6.metric("Débit", f"{throughput:.2f} img/s")

st.divider()

df_recent = df[
    df["processTime"].dt.floor("min") > now - timedelta(minutes=5)
]

df_per_sec = (
    df_recent
    .set_index("processTime")
    .resample("1s")
    .agg(
        mean_delay=("delay_seconds", "mean"),
        images_processed=("path", "count")
    ).dropna()
    .reset_index()
)

fig1 = px.line(
    df_per_sec,
    x="processTime",
    y="images_processed",
    title="Images traitées par seconde"
)

fig2 = px.line(
    df_per_sec,
    x="processTime",
    y="mean_delay",
    title="Latence moyenne par seconde"
)

st.plotly_chart(fig1, width='stretch')
st.plotly_chart(fig2, width='stretch')

st.divider()

df_new_recent = df[
    df["modificationTime"].dt.floor("min") > now - timedelta(minutes=5)
]

df_new_per_sec = (
    df_new_recent
    .set_index("modificationTime")
    .resample("1s")
    .agg(
        new_images=("path", "count")
    ).dropna()
    .reset_index()
)

fig = px.line(
    df_new_per_sec,
    x="modificationTime",
    y="new_images",
    title="Nouvelles images par seconde"
)

st.plotly_chart(fig, width='stretch')

st.divider()

df_recent["minute"] = df_recent["processTime"].dt.floor("min")
throughput_df = df_recent.groupby("minute").size().reset_index(name="images")

fig = px.bar( throughput_df, x="minute", y="images", title="Images traitées par minute" )
st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.histogram( df, x="delay_ms", nbins=40, title="Distribution des temps de traitement" )
st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.box( df, x="true_label", y="delay_ms", color="true_label", title="Temps de traitement par catégorie" )
st.plotly_chart(fig, width='stretch')