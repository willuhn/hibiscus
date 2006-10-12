function openWindow(url,width,height)
{

  var params = "scrollbar=auto,scrollbars=yes,menubar=no,toolbar=no,status=no,resizable=no";

  if (isNaN(width) == false)
    params += ",width="+width;

  if (isNaN(height) == false)
    params += ",height="+height;

  mywindow = window.open(url,Math.round(Math.random() * 1000000), params);
  mywindow.focus();
}
