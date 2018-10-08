@echo off
 FOR /f %%L IN ('dir /b src\compiladorpascal\files\bateria\*.pas') do java -jar dist/CompiladorPascal.jar src\compiladorpascal\files\bateria\%%L
pause