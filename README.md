Esta aplicación accede a diversas APIs de la Nasa:

    APOD -> Astronomy Picture of the Day: Imágen astronómica del día.
        Es una selección diaria, realizada por la Nasa, de una fotografía sobre temática astronómíca. Las imágenes del día se descargan al acceder a la sección, o através de un
        servicio automático periódico. También se pueden descargar imágenes correspondientes a días anteriores (posteriores a la fecha de inicio del servicio: 16/06/1995).
        Los datos de las imágenes se guardan automáticamente en una base de datos del dispositivo, pudiendose borrar las que no interesen. 

    EPIC -> Earth Polychromatic Imaging Camera: Imágen policromática de la Tierra.
        Conjunto de imágenes de la Tierra tomadas por el satélite DSCOVR de la Nasa, en órbita geoestacionaria. Por defecto se descarga una versión mejorada de las mismas, pero
        se puede seleccionar la versión natural. También se pueden descargar las imágenes de días pasados.

    MARS ROVERS PHOTOS -> Imágenes tomadas por los rovers de la Nasa sobre la superficie de Marte. Se puede seleccionar el rover de entre los cuatro ahora presentes en la API: 
        Perseverance (activo), Curiosity (activo), Opportunity (finalizado) o Spirit (finalizado). Al acceder a la sección, en los campos de día, mes y año aparece la fecha de 
        amartizaje de la nave, pudiéndose descargar a partír de esa fecha (más o menos; hay algunas no disponibles por diversos motivos). Si no se introduce una fecha (terrestre)
        concreta, se descargará las últimas recibidas. Éstas imágenes se pueden guardar en una base de datos del dispositivo. También se lleva un registro de las fechas vistas
        (por si se desea ir viendo pero no repetirse). También se puede acceder a los manifiestos de los rover con diversa información e imágenes de los mismos.

    Todos los datos almacenados en la base de datos alojada en el dispositivo se puede volcar a unos archivos .json (uno para los apods, otro para las fotos de Marte, y un 
    tercero para las fechas visitadas), para permitir su traslado a otros dispositivos.

    Todas las imágenes se alojan en marcos que con un toque (tap) se pasan a un contenedor con capacidad de zoom y scroll. Es la biblioteca ZoomableImageView desarrollada por
    Ravi Koradiya disponible en Github bajo licencia Apache.

    La aplicación requiere autenticación con una API_KEY para acceder a las APIs que puede tramitarse en en la web api.nasa.gov. Ésta versión cuenta con una DEMO-KEY que 
    permite su exploración, pero para un uso mayor se recomienda una API_KEY propia, a introducir en la constante pertinente del archivo NasaApiService.kt.