<?php

namespace App;

use Illuminate\Database\Eloquent\Model;
use SahusoftCom\EloquentImageMutator\EloquentImageMutatorTrait;

class Frigo
{

    /**
     * The photo fields should be listed here.
     *
     * @var array
     */
    protected $image_fields = ['img1', 'img2', 'img3'];

    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
    protected $fillable = [
        'id', 'name', 'count', 'count_type', 'production_date', 'expiration_date', 'note', 'barcode'
    ];
}
