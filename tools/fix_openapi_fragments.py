from typing import Dict
from argparse import ArgumentParser
from glob import glob
from yaml import load, dump, CLoader, CDumper


def get_and_unset(openapi: Dict[str, str], property_name: str) -> str:
    """Look up the property_name by name in openapi, return it and unset it in
    openapi."""
    result = openapi[property_name]
    del openapi[property_name]
    return result


def set_info_property(info: Dict[str, str], openapi: Dict[str, str],
                      property_name: str):
    """Set `property` in `info` to its value in `openapi`, if it's present in
    `openapi`"""
    if property_name in openapi:
        info[property_name] = get_and_unset(openapi, property_name)


def fix_single_file(file_path: str, openapi_version: str):
    """Convert a single fragment into a valid document."""
    with open(file_path, encoding='UTF-8') as file:
        contents = load(file, Loader=CLoader)
    info = {}
    set_info_property(info, contents, 'title')
    set_info_property(info, contents, 'x-copyright')
    set_info_property(info, contents, 'x-owningEntity')
    set_info_property(info, contents, 'x-release')
    contents['info'] = info
    contents['openapi'] = openapi_version
    with open(file_path, 'w', encoding='UTF-8') as file:
        dump(contents, file, Dumper=CDumper)


def main(directory: str):
    """Convert every OpenAPI document fragment file in `directory` to a valid
    OpenAPI document"""
    files = glob(directory + '/*')
    root = directory + '/openapi.yaml'
    files.remove(root)
    with open(root, encoding='UTF-8') as file:
        openapi = load(file, Loader=CLoader)
        openapi_version = openapi['openapi']
    for file in files:
        fix_single_file(file, openapi_version)


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('directory', help='OpenAPI Spec directory')

    args = parser.parse_args()
    main(args.directory)
