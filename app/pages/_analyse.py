import streamlit as st
import pandas as pd
import plotly.express as px
from utils.data_loader import load_json_folder
from streamlit_autorefresh import st_autorefresh
from pathlib import Path


st_autorefresh(
    interval=3000,
    key="refresh_images"
)


df = load_json_folder()


st.title("📸 Analyse des images")


# KPI

c1,c2,c3 = st.columns(3)

c1.metric(
    "Images analysées",
    len(df)
)

c2.metric(
    "Confiance moyenne",
    f"{df.confidence.mean():.2%}"
)

c3.metric(
    "Dernière image",
    df.processTime.max().strftime("%Y-%m-%d %H:%M:%S")
)

df["filename"] = [val[-1] for val in df.path.str.split("_")]

st.header(len(df.filename.unique()))


# Distribution classes

fig = px.histogram(
    df,
    x="predicted_class",
    color="predicted_class",
    title="Répartition des déchets"
)

st.plotly_chart(
    fig,
    use_container_width=True
)


fig = px.histogram(
    df,
    x="true_label",
    color="true_label",
    title="Répartition des déchets en entrée"
)

st.plotly_chart(
    fig,
    use_container_width=True
)


df_correct = (
    df["is_correct"]
    .value_counts()
    .rename_axis("Résultat")
    .reset_index(name="Nombre")
)

# Pour avoir des labels plus lisibles
df_correct["Résultat"] = df_correct["Résultat"].map({
    True: "Correct",
    False: "Incorrect"
})

fig = px.pie(
    df_correct,
    values="Nombre",
    names="Résultat",
    title="Répartition des prédictions correctes",
    hole=0.4
)

fig.update_traces(
    textposition="inside",
    textinfo="percent+label"
)

st.plotly_chart(
    fig,
    use_container_width=True
)



# Table interactive

st.subheader(
    "Résultats"
)


st.dataframe(
    df,
    use_container_width=True
)