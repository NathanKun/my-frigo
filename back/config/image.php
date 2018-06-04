<?php

return array(
    'library'     => 'gd',
    'upload_dir'  => 'uploads',
    'assets_upload_path' => 'storage/app/uploads',
    'quality'     => 85,
    'default'     => [
        'url'     => 'https://placehold.it/150x150',
        'width'   => 150,
        'height'  => 150
    ],
    'dimensions'  => [
        ['300', '300',  true,   85, 'profile'],
        ['800', '600',  false,  85, 'large']
    ]
);
