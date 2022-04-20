É necessário que o arquivo gdb seja convertido para FDB,
usando a ferramenta FDBConverter, que é gratuita.

linha de comando:

java -jar Convertegdb.jar caminho_diretorio_arquivos_gdb

Cada tabela será extraída e salva em csv. No final todos
os arquivos estarão dentro do arquivo zipado, e os arquivos
csv temporários criados excluídos do sistema.
