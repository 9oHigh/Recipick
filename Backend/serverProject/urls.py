from django.contrib import admin
from django.urls import path
from .views import userViews
from django.conf.urls import include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('users', userViews.user_list),
    path('users/validate', userViews.userValidate),
    path('users/secession', userViews.userSecession),
    path('users/login', userViews.login),
    path('users/access', userViews.access),
    path('users/reissuance', userViews.reissuance),
    path('auth', include('rest_framework.urls', namespace='rest_framework'))
]
