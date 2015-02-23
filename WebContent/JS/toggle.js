function hide_element(element_id)
{
  var h = document.getElementById(element_id);
  h.style.display = 'none';
}

function zoom_in(element_id)
{
  var e = document.getElementById(element_id);
  var w = e.offsetWidth;
  var h = e.offsetHeight; 
  if (typeof(w) == undefined)
  {
    zoom_in('div' + element_id);
  }
  // alert(element_id + ": " + h + ",  " + w);
  e.style.width = 1.4 * w;
  e.style.height = 1.4 * h; 
}

function zoom_out(element_id)
{
  var e = document.getElementById(element_id);
  var w = e.offsetWidth;
  var h = e.offsetHeight; 

  if (typeof(w) == undefined)
  {
    zoom_out('div' + element_id);
  }

  e.style.width =  0.7 * w;
  e.style.height = 0.7 * h; 
}

function show_element(element_id)
{
  var h = document.getElementById(element_id);
  h.style.display = 'block';
}

function toggle_element(element_id)
{
  var h = document.getElementById(element_id);
  if (h.style.display == 'block')
  {
    h.style.display = 'none';
  } else
  {
    h.style.display = 'block';
  }
}

