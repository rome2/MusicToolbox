

// Get the mouse event cursor position for a canvas element.
// canvas: the canvas.
// e: the mouse event parameter.
// Returns an object with the x and y coordinates.
function getCanvasCursorPosition(canvas, e) {

    var x;
    var y;

    // In modern browsers it's simple:
    if (e.pageX != undefined && e.pageY != undefined) {
      x = e.pageX;
      y = e.pageY;
    }
    else {
      // IE < 12 doesn't have pageXY in the event so calc it ourselves:
      x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
      y = e.clientY + document.body.scrollTop  + document.documentElement.scrollTop;
    }

    // X and Y are now relative to the document. Move them to the canvas:
    x -= canvas.offsetLeft;
    y -= canvas.offsetTop;

    // Done:
    return {
      x: x,
      y: y
    };
}

// Make sure that a canvas is High DPI aware. Sets up scaling as needed.
//
// Example:
//   var ctx = setupCanvas(document.querySelector('.my-canvas'));
//   ctx.lineWidth = 5;
//   ctx.beginPath();
//   ctx.moveTo(100, 100);
//   ctx.lineTo(200, 200);
//   ctx.stroke();
function setupCanvas(canvas) {

  // Get the device pixel ratio, falling back to 1:
  var dpr = window.devicePixelRatio || 1;

  // Get the size of the canvas in CSS pixels:
  var rect = canvas.getBoundingClientRect();

  // Give the canvas pixel dimensions of their CSS size * the device pixel ratio:
  canvas.width  = rect.width * dpr;
  canvas.height = rect.height * dpr;
  var ctx = canvas.getContext('2d');

  // Scale all drawing operations by the dpr, so you don't have to worry about the difference.
  ctx.scale(dpr, dpr);
  return ctx;
}
