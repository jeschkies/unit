from setuptools import find_packages, setup

setup(name='unit',
      version='0.0.1',
      packages=find_packages(),
      package_data={
          '': ['templates/*.html', 'static/*.*']
      },
      include_package_data=True)
