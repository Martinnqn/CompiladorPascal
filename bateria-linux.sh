for file in src/compiladorpascal/files/bateria/*.pas
do
	java -jar dist/CompiladorPascal.jar $file
done
