function getUrlParameters()
{
	var search = window.location.search.substr(1);
	var cgiVars = search.split("&");
	cgiAssoc = new Array();
	for(i in cgiVars)
	{
		tmp = cgiVars[i].split("=");
		cgiAssoc[unescape(tmp[0])] = unescape(tmp[1]);
	}
	return cgiAssoc;
}

