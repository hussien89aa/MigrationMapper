import jellyfish
from .TextNLP import TextNLP
class CSimilarity:
        def get_cosine(self,text1, text2):
             return jellyfish.jaro_distance(text1, text2)


## Test code
def main():
    text1 = "create mock request type builder ||| builder create multiple mock"
    text2 = "create mock object class interface ||| example javadoc mockito class"
    textNLP = TextNLP()
    str= "Create a named mock of the request type from this builder. The same builder can be called to create multiple mocks.";

    cosine = CSimilarity().get_cosine(textNLP.getText(text1), textNLP.getText(text2))
    print(cosine)


if __name__ == '__main__':main()