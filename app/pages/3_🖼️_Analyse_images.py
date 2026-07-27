import streamlit as st
import plotly.express as px
from streamlit_autorefresh import st_autorefresh
from utils.data_loader import load_json_folder

st_autorefresh(interval=3000, key="refresh_images")

df = load_json_folder()

st.title("🖼️ Analyse des images")

col1, col2 = st.columns(2)

with col1:
    fig = px.histogram( df, x="mean_pixel", nbins=40, title="Luminosité moyenne" )
    st.plotly_chart(fig, width='stretch')

with col2:
    fig = px.histogram( df, x="std_pixel", nbins=40, title="Contraste (écart-type)" )
    st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.scatter( df, x="mean_pixel", y="confidence", color="is_correct", title="Luminosité vs confiance", hover_data=["true_label", "predicted_class"] )
st.plotly_chart(fig, width='stretch')

fig = px.scatter( df, x="std_pixel", y="confidence", color="is_correct", title="Contraste vs confiance", hover_data=["true_label", "predicted_class"] )
st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.scatter( df, x="size_kb", y="confidence", color="true_label", title="Taille des images vs confiance" )
st.plotly_chart(fig, width='stretch')

st.divider()

corr = df[[ "mean_pixel", "std_pixel", "min_pixel", "max_pixel", "confidence", "size_kb", "delay_seconds" ]].corr()

fig = px.imshow( corr, text_auto=".2f", title="Corrélations des variables", aspect="auto" )
st.plotly_chart(fig, width='stretch')