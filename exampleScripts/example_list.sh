wget -O- 'http://localhost:8080/LMServer/LMServer?action=LIST' | perl -pe 's/},{/},\n{/g'
