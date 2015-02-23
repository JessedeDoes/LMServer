function hide_element(element_id)
{
  var h = document.getElementById(element_id);
  h.style.display = 'none';
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

