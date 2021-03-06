from django.shortcuts import render
from django.http import HttpResponse
from django.http import JsonResponse
from django.contrib.auth.hashers import BCryptPasswordHasher, check_password, make_password
import json
from .models import Player, Song, BeatMap, Play
import os

# Create your views here.
def index(request):
    return HttpResponse("Hello, you have reached the API.")

def register(request):
    
    if request.method == "GET":
        return HttpResponse("Hello, you have reached the API registry")
    
    data = json.loads(request.body)

    if 'username' not in data or data['username'] == '':
        return HttpResponse("empty username")
    
    if 'password' not in data or data['password'] == '':
        return HttpResponse("empty password")
     
    if Player.objects.filter(username= data['username'] ).exists():
        return HttpResponse("duplicate user.")
    
    create_user(data['username'], data['password'])
    
    
    return HttpResponse("user registered!")

def create_user(u_name, pwd):
    new_user = Player(username=u_name, pwd_hash=make_password(pwd, hasher='pbkdf2_sha256'))
    new_user.save()

def download_song(request):
    song_file = ''

    if request.method == "GET":
        return HttpResponse('')
    
    data = json.loads(request.body)

    if 'name' not in data or data['name'] == '':
        return HttpResponse('empty name')
    
    # this should actually be able to catch any injection attacks but
    elif Song.objects.filter(name = data['name']).exists() is not True:
        return HttpResponse('Song does not Exist')
    
    song_file = data['name'].replace('.', '').replace('|', '').replace('*','').replace('?', '').replace('~','') + '.mp3'
    
    with open(os.path.join(os.path.dirname(__file__), "Songs/{}".format(song_file) ), "rb" ) as f:
        song_data = f.read()
    
    response = HttpResponse(song_data, content_type='application/mp3')
    response['Content-Disposition'] = 'attachment; filename="song.mp3"'
    
    return response    

def download_beatmap(request):
    song_file = ''
    data = json.loads(request.body)

    if request.method == "GET":
        return HttpResponse('')
    
    if 'name' not in data or data['name'] == '':
        return HttpResponse('empty name')
    
    # this should actually be able to catch any injection attacks but
    elif Song.objects.filter(name = data['name']).exists() is not True:
        return HttpResponse('Song {} does not Exist'.format(data['name']))
    
    song_file = data['name'].replace('.', '').replace('|', '').replace('*','').replace('?', '').replace('~','') + '.json'
    
    with open(os.path.join(os.path.dirname(__file__), "Songs/{}".format(song_file) ) ) as f:
        song_data = json.load(f)
    return JsonResponse(song_data)

def record_play(request):
    if request.method == "GET":
        return HttpResponse('')

    data = json.loads(request.body)
    
    if 'username' not in data or data['username'] == '' or Player.objects.filter(username= data['username'] ).exists() is not True:
        return HttpResponse("invalid username")
    # should we check a password for validation?
    elif 'beatmap' not in data or data['beatmap'] == '' or BeatMap.objects.filter(id= data['beatmap'] ).exists() is not True:
        return HttpResponse('invalid beat_map')
    elif 'score' not in data or data['score'] == '':
        return HttpResponse('empty score')
    
    if 'rating' not in data or data['rating'] == '':
        rating = None
    else:
        rating = data['rating']
    
    create_play(data['username'],  data['beatmap'], data['score'], rating)
    body = {'message': 'Score Registered!'} 
    return JsonResponse(body)


def create_play(username, beatmap, score, rating):
    uid = Player.objects.filter(username=username)[0].id
    new_play = Play(player_id = uid, beat_map_id = beatmap, score = score, rating = rating)
    new_play.save()

    
    

