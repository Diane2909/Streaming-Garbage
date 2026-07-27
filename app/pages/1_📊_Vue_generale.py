import streamlit as st
import plotly.express as px
from streamlit_autorefresh import st_autorefresh
from utils.data_loader import load_json_folder
from utils.metrics import compute_kpis

st_autorefresh(interval=3000, key="refresh_general")

df = load_json_folder()
kpi = compute_kpis(df)

st.title("📊 Vue générale")

c1, c2, c3, c4 = st.columns(4)
c1.metric("Images analysées", kpi["total"])
c2.metric("Accuracy", f"{kpi['accuracy']:.2%}")
c3.metric("Confiance moyenne", f"{kpi['confidence']:.2%}")
c4.metric("Temps moyen", f"{kpi['delay']:.2f}s")

st.divider()

col1, col2 = st.columns(2)

with col1:
    fig = px.histogram( df, x="true_label", color="true_label", title="Répartition des images par dossier" )
    st.plotly_chart(fig, width='stretch')

with col2:
    fig = px.pie( df, names="is_correct", hole=0.4, title="Prédictions correctes / incorrectes" )
    st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.line( df.sort_values("processTime"), x="processTime", y="delay_seconds", title="Evolution du temps de traitement" )
st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.histogram( df, x="confidence", nbins=30, title="Distribution des confiances" )
st.plotly_chart(fig, width='stretch')