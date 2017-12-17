from aiohttp import web


async def handle(request):
    name = request.match_info.get('name', "Anonymous")
    text = 'Hello, {}'.format(name)
    return web.Response(text=text)


def app():
    app_ = web.Application()
    app_.router.add_get('/', handle)
    app_.router.add_get('/{name}', handle)
    return app_


def main():
    web.run_app(app())


if __name__ == '__main__':
    main()
