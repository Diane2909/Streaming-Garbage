import streamlit as st
import plotly.express as px
import pandas as pd
from sklearn.metrics import confusion_matrix
from streamlit_autorefresh import st_autorefresh
from utils.data_loader import load_json_folder

st_autorefresh(interval=3000, key="refresh_quality")

df = load_json_folder()

st.title("Qualité du modèle")

fig = px.histogram(
    df,
    x="predicted_class",
    color="is_correct",
    title="Répartition des déchets"
)

st.plotly_chart(
    fig,
    width='stretch'
)

acc_by_class = ( df.groupby("predicted_class")["is_correct"] .mean() .reset_index() )

fig = px.bar( acc_by_class, x="predicted_class", y="is_correct", text_auto='.1%', title="Accuracy par classe" )
fig.update_yaxes(tickformat=".0%")
st.plotly_chart(fig, width='stretch')

st.divider()

labels = sorted(df["true_label"].unique())
cm = confusion_matrix(df["true_label"], df["predicted_class"], labels=labels)
cm_df = pd.DataFrame(cm, index=labels, columns=labels)

fig = px.imshow( cm_df, text_auto=True, title="Matrice de confusion", aspect="auto" )
st.plotly_chart(fig, width='stretch')

st.divider()

fig = px.box( df, x="predicted_class", y="confidence", color="predicted_class", title="Confiance par classe prédite" )
st.plotly_chart(fig, width='stretch')

st.divider()

st.subheader("Erreurs du modèle")
errors = df[df["is_correct"] == False]
st.dataframe( errors[["path", "true_label", "predicted_class", "confidence"]], width='stretch' )

st.subheader("Images les plus difficiles")
hardest = df.sort_values("confidence").head(20)
st.dataframe( hardest[["path", "true_label", "predicted_class", "confidence"]], width='stretch' )