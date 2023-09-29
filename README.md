<h1 align="center" id="title">Nasa Apis</h1>

<p id="description">Aplicación para el acceso a diversas APIs de la Nasa</p>

<h2>Características</h2>

<p>APOD -> Astronomy Picture of the Day: Imágen astronómica del día.<br/>Es una selección diaria, realizada por la Nasa, de una fotografía sobre temática astronómíca. Las imágenes del día se descargan al acceder a la sección, o através de un servicio automático periódico. También se pueden descargar imágenes correspondientes a días anteriores (posteriores a la fecha de inicio del servicio: 16/06/1995). Los datos de las imágenes se guardan automáticamente en una base de datos del dispositivo, pudiendose borrar las que no interesen. </p>

<p>EPIC -> Earth Polychromatic Imaging Camera: Imágen policromática de la Tierra.<br/>Conjunto de imágenes de la Tierra tomadas por el satélite DSCOVR de la Nasa, en órbita geoestacionaria. Por defecto se descarga una versión mejorada de las mismas, pero se puede seleccionar la versión natural. También se pueden descargar las imágenes de días pasados.</p>

<p>MARS ROVERS PHOTOS -> Imágenes tomadas por los rovers de la Nasa sobre la superficie de Marte.<br/>Se puede seleccionar el rover de entre los cuatro ahora presentes en la API:<br/>
Perseverance (activo), Curiosity (activo), Opportunity (finalizado) o Spirit (finalizado). Al acceder a la sección, en los campos de día, mes y año aparece la fecha de amartizaje de la nave, pudiéndose descargar a partír de esa fecha (más o menos; hay algunas no disponibles por diversos motivos). Si no se introduce una fecha (terrestre) concreta, se descargará las últimas recibidas. Éstas imágenes se pueden guardar en una base de datos del dispositivo. También se lleva un registro de las fechas vistas (por si se desea ir viendo pero no repetirse). Se puede acceder a los manifiestos de los rover con diversa información e imágenes de los mismos.</p>

<p>Todos los datos almacenados en la base de datos local del dispositivo se puede volcar a un archivo .json, como backup y para permitir su traslado a otros dispositivos.</p>

<p>La aplicación requiere autenticación con una API_KEY para acceder a las APIs que puede tramitarse en en la web api.nasa.gov. Para explorar la aplicación, se puede usar como api_key "DEMO-KEY", introduciendo este valor en la constante API_KEY del archivo NasaApiService.kt. En caso de usar una clave propia, en el archivo <i>local.properties</i> reflejar la asignación <i>apiKey="TU_API_KEY"</i></p>

<h2>💻 Realizado con</h2>

*   Kotlin
*   MVVM
*   LiveData
*   DataBinding
*   Room
*   Retrofit y Moshi


<h2>Atribuciones</h2>

<p>Todas las imágenes se alojan en marcos que con un toque (tap) se pasan a un contenedor con capacidad de zoom y scroll. Es la biblioteca ZoomableImageView desarrollada por <a target="_blank" href="https://github.com/RaviKoradiya/ZoomableImageView">Ravi Koradiya</a> disponible en Github bajo licencia Apache.</p>
