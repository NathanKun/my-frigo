<?php

namespace App\Models;

use Eloquent as Model;
use Illuminate\Database\Eloquent\SoftDeletes;
use SahusoftCom\EloquentImageMutator\EloquentImageMutatorTrait;

/**
 * Class Food
 * @package App\Models
 * @version June 4, 2018, 6:33 pm UTC
 *
 * @property string name
 * @property integer count
 * @property integer count_type
 * @property date production_date
 * @property date expiration_date
 * @property string note
 * @property string img1
 * @property string img2
 * @property string img3
 * @property string barcode
 */
class Food extends Model
{
    use SoftDeletes;
   	use EloquentImageMutatorTrait;

    public $table = 'foods';


    protected $dates = ['deleted_at'];


    /**
     * The photo fields should be listed here.
     *
     * @var array
     */
    protected $image_fields = ['img1', 'img2', 'img3'];


    public $fillable = [
        'name',
        'count',
        'count_type',
        'production_date',
        'expiration_date',
        'note',
        'barcode',
        'is_history'
    ];

    /**
     * The attributes that should be casted to native types.
     *
     * @var array
     */
    protected $casts = [
        'name' => 'string',
        'count' => 'integer',
        'count_type' => 'integer',
        'production_date' => 'date',
        'expiration_date' => 'date',
        'note' => 'string',
        'img1' => 'string',
        'img2' => 'string',
        'img3' => 'string',
        'barcode' => 'string'
    ];

    /**
     * Validation rules
     *
     * @var array
     */
    public static $rules = [
        'count' => 'numeric'
    ];


}
