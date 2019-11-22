#using NLTK library, we can do lot of text preprocesing
import nltk,string
#nltk.download('punkt')
#nltk.download('stopwords')
#nltk.download('wordnet')
from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
from nltk.stem import PorterStemmer, WordNetLemmatizer

import pandas as pd
import numpy as np

# remove  non alphabetic characters.
remove_punct_dict = dict((ord(punct), None) for punct in string.punctuation)

#TF-IDF
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

class TextNLP:
     def getText(self,text):
        print(text)
        #function to split text into word
        #tokens = word_tokenize(text)
        #print(tokens)
        tokens = word_tokenize(text.translate(remove_punct_dict))
        # remove  non alphabetic characters.
        #tokens=[word.lower() for word in tokens if word.isalpha()]
        print(tokens)

        #Removing stop words frequent words
        stop_words = set(stopwords.words('english'))
        tokens = [word for word in tokens if not word in stop_words]
        print(tokens)


        #Stemming
        #porter = PorterStemmer()
        #stems =  ""
        #for t in tokens:
         #   stems+=porter.stem(t) + " "
        #Lemmatizer
        wnl = WordNetLemmatizer()
        return " ".join([wnl.lemmatize(i) for i in tokens])

     def TFIDF(self,corpus):
            vectorizer = TfidfVectorizer(min_df=1)
            tfidf_matrix_train = vectorizer.fit_transform(corpus)
            idf = vectorizer.idf_
            print(idf )
            print("-------")
            print( dict(zip(vectorizer.get_feature_names(), idf)))
            print(tfidf_matrix_train )
            print("-------")
            print(tfidf_matrix_train.toarray())
            print("-------")
            print(tfidf_matrix_train[0:1])
            print("-------")
            print(tfidf_matrix_train[1:2])
            print(cosine_similarity(tfidf_matrix_train[0:1], tfidf_matrix_train[1:2]))


     def get_tfidf(self,docs, ngram_range=(1,1), index=None):
        vect = TfidfVectorizer(stop_words='english', ngram_range=ngram_range)
        tfidf = vect.fit_transform(docs).todense()
        return pd.DataFrame(tfidf, columns=vect.get_feature_names(), index=index).T


## Test code
def main():
    textNLP = TextNLP()
    str= "Create a named mock of the request type from this builder. The same builder can be called to create multiple mocks.";
    #print(str)
    newText= textNLP.getText(str)
    print(newText)
    str= "Creates mock object of given class or interface. See examples in javadoc for Mockito class";
    #print(str)
    newText= textNLP.getText(str)
    print(newText)

    corpus= ["create mock request type builder ||| builder create multiple mock",
                  "create mock object class interface ||| example javadoc mockito class"]

    textNLP.TFIDF(corpus)

    document_names = ['Doc {:d}'.format(i) for i in range(len(corpus))]
    print(textNLP.get_tfidf(corpus, ngram_range=(1,1), index=document_names))


if __name__ == '__main__':main()