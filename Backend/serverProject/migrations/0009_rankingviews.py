# Generated by Django 3.0 on 2021-11-09 13:59

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('serverProject', '0008_auto_20211108_1909'),
    ]

    operations = [
        migrations.CreateModel(
            name='RankingViews',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('views', models.IntegerField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('rId', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='serverProject.R_info')),
            ],
        ),
    ]