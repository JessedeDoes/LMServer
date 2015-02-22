// ensureLargeEnough.js - (C) Copyright 2007, Instituut voor Nederlandse Lexicologie

var widthRequired = 993, heightRequired = 500;

function ensureLargeEnough()
{
	if (!isLargeEnough())
	{
		if(confirm('Uw browservenster is te klein voor de applicatie. Wilt u de browser maximaliseren?'))
		{
			tryMaximize();
		}
	}
}

function isLargeEnough()
{
	if (top.window.innerHeight && top.window.innerWidth)
	{
		return top.window.innerWidth >= widthRequired && top.window.innerHeight >= heightRequired;
	}
	else if (document.body.clientWidth && document.body.clientHeight)
	{
		return document.body.clientWidth >= widthRequired && document.body.clientHeight >= heightRequired;
	}
	return true;
}


function tryMaximize()
{
	if (top.window.moveTo)
	{
		top.window.moveTo(0,0);
	}
	if (screen.availWidth && screen.availHeight)
	{
		if (top.window.resizeTo) 
		{
			top.window.resizeTo(screen.availWidth,screen.availHeight);
		}
		else if (top.window.outerHeight && top.window.outerWidth)
		{
			if (top.window.outerHeight < screen.availHeight || top.window.outerWidth < screen.availWidth)
			{
				top.window.outerHeight = screen.availHeight;
				top.window.outerWidth = screen.availWidth;
			}
		}
		else
		{
			alert('Sorry, het is niet mogelijk om uw browser automatisch te maximaliseren.\n\n'+
				'Maximaliseer a.u.b. zelf uw browser en herlaad de pagina.');
			return;
		}
	}
	if (!isLargeEnough())
	{
		alert('Deze pagina kan uw browser niet automatisch maximaliseren. \n\n'+
			'Dit kan gebeuren als u meerdere webpagina\'s in tabbladen geopend hebt,\n'+
			'of als uw beveiligingsinstellingen op "Hoog" staan.\n\n'+
			'Maximaliseer a.u.b. zelf uw browser en herlaad de pagina.');
	}
}


