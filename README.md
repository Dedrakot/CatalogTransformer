В репозитории нет каталога, изображений, выборки для тренировки нейронной сети и самой натренированной сети.

С аргументами не заморачивался, как правило, в начале исполняемого кода стоит список объявлений с используемыми файлами.

Для работы с изображениями используется opencv. В принципе, если есть проблемы с её установкой, можно от неё отказаться.
Читать изображения может и стандартная библиотека, для сжатия изображения можно будет воспользоваться аффинными преобразованиями
(см. java.awt.image). Также, вероятно, при помощи opencv можно сделать более эффективное и универсальное решение (не исследовал этот вопрос).

Пошаговая инструкция:
1. Скачайте xml каталога ("tmp/products.xml)

2. Загрузите изображения из каталога.
LoadProductCatalogImages::loadImages
По умолчанию сохранит в "tmp/images/catalog"
По умолчанию в каталоге берутся model_image и cutout_image в продукте и варианте и скачиваются.
Префикс к ссылке на изображение по умолчанию "http:".

3. Найдите одно изображение соответствующее логотипу.
Путь для сохранения по умолчанию - "tmp/hm.jpg".

4. Сделайте предварительную выборку.
В Main::main раскомментируйте строчки создания матчера по гистограмме
```
final ImageMatcher matcher = createHistogramMatcher("tmp/hm.jpg");
``` 
Запуск приведёт к созданию "tmp/result.txt".
Он должен быть достаточно неплох, но всё-таки может содержать некорректно выбранные изображения.
Для разъяснения работы можно посмотреть пример TestImageCompare::sampleCode

5. Скопировать полученные файлы можно будет при помощи CopyResultList::copyImagesFromResults.
Файлы, соответствующие строкам из result.txt будут скопированы в "tmp/images/flat".

6. Если визуально отклонений немного, то можно отредактировать result.txt вручную
и перейти к шагу 11.

7. Переместите некорректные изображения из полученного списка в отдельную директорию (по умолчанию "tmp/images/flat2")
и добавьте в неё же ещё произвольных не являющихся логотипом изображений из каталога. Хотя бы пару десятков.

8. Подготовьте изображения для обучения сети.
Разбираться с тем, как правильно сравнивать изображения не стал (верю, что можно сделать лучше).
Нейронная сеть берёт для проверки только часть изображения. В каталоге всё было по центру, поэтому просто вырезается 
квадрат в треть ширины, затем он сжимается до размера 64x64 и подаётся на вход нейронной сети. При тренировке много
повторений и повторять эту операцию много раз я не стал, просто подавал на вход центры изображений.
Для этого есть тест SquareFound::scaleResults. Надо будет обработать обе папки: с образцами ("tmp/images/flat") и произвольными
изображениями не соответствующими образцу ("tmp/images/flat2").

9. Натренировать сеть. Тест NeuralNetworkTest::trainAndSave.
По умолчанию папка "tmp/images/square" используется тренером для образцов,
а "tmp/images/square2" для несоответствий образцу.
коэффициенты нейросети будут записаны в "tmp/trained".
Смотрите стандартный вывод, сеть должна правильно определять все образцы.
Можно проверить это запустив NeuralNetworkTest::match

10. Запустить Main с матчером на основе нейросети.
```final ImageMatcher matcher = createNeuralMatcher("tmp/trained");```
Будет сформирован новый "tmp/result.txt"

11. Почистить каталог
TransformCatalog::main удалит аттрибуты содержащие изображения из results.txt согласно main/resources/removeImages.xsl
По умолчанию результат будет записан в "changedProducts.xml".
