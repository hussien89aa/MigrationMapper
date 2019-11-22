from django.http import JsonResponse
from django.views.generic import TemplateView
from .CSimilarity import CSimilarity
from .TextNLP import TextNLP
import json
from django.views.decorators.csrf import csrf_exempt

class HomePageView(TemplateView):
    template_name = 'home.html'
    #return render(request, 'home.html', context)




class AboutPageView(TemplateView):
 template_name = 'about.html'

@csrf_exempt
def nlp(request):

        json_data=json.loads(request.body.decode("utf-8"))
        print(json_data)
        text1NLP=TextNLP().getText(json_data["text1"])
        text2NLP=TextNLP().getText(json_data["text2"])
        score = CSimilarity().get_cosine(text1NLP, text2NLP)

        context = {'score':score,
                   'text1NLP': text1NLP,
                   'text2NLP': text2NLP}
        print(context)
        #return render(request, 'year_archive.html', context)
        return JsonResponse(context)



