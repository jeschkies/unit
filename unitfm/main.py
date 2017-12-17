from aiohttp import web
import xml.etree.ElementTree as ET


async def handle(request):
    name = request.match_info.get('name', "Anonymous")
    text = 'Hello, {}'.format(name)
    return web.Response(text=text)


async def view_junit(request):
    junit = ET.parse('pytest.xml').getroot()
    page = 'Errors: {}'.format(junit.get('errors'))
    return web.Response(text=page)

def app():
    app_ = web.Application()
    app_.router.add_get('/', handle)
    app_.router.add_get('/view', view_junit)
    return app_


def main():
    web.run_app(app())


if __name__ == '__main__':
    main()
