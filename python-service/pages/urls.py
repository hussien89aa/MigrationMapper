from django.urls import path
from . import views
from .views import AboutPageView, HomePageView

urlpatterns = [
    path('nlp/', views.nlp),
    path('about/', AboutPageView.as_view(), name='about'),
    path('', HomePageView.as_view(), name='home'),
]
