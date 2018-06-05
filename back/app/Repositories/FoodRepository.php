<?php

namespace App\Repositories;

use App\Models\Food;
use InfyOm\Generator\Common\BaseRepository;
use Illuminate\Support\Facades\Input;

/**
 * Class FoodRepository
 * @package App\Repositories
 * @version June 4, 2018, 6:33 pm UTC
 *
 * @method Food findWithoutFail($id, $columns = ['*'])
 * @method Food find($id, $columns = ['*'])
 * @method Food first($columns = ['*'])
*/
class FoodRepository extends BaseRepository
{
    /**
     * @var array
     */
    protected $fieldSearchable = [
        'id',
        'name',
        'count',
        'count_type',
        'production_date',
        'expiration_date',
        'note',
        'img1',
        'img2',
        'img3',
        'barcode'
    ];

    /**
     * Configure the Model
     **/
    public function model()
    {
        return Food::class;
    }

    /**
     * @override
     */
    public function create(array $attributes)
    {
        // Have to skip presenter to get a model not some data
        $temporarySkipPresenter = $this->skipPresenter;
        $this->skipPresenter(true);
        $model = parent::create($attributes);
        $this->skipPresenter($temporarySkipPresenter);

        if (Input::hasFile('img1')) {
            $model->img1 = Input::file('img1');
        }
        if (Input::hasFile('img2')) {
            $model->img2 = Input::file('img2');
        }
        if (Input::hasFile('img3')) {
            $model->img3 = Input::file('img3');
        }

        $model = $this->updateRelations($model, $attributes);
        $model->save();

        // create history
        $historyModel = $model->replicate();
        $historyModel->is_history = true;
        $historyModel->history_id = $model->id;
        $historyModel->save();

        return $this->parserResult($model);
    }


    /**
     * @override
     */
    public function update(array $attributes, $id)
    {
        // Have to skip presenter to get a model not some data
        $temporarySkipPresenter = $this->skipPresenter;
        $this->skipPresenter(true);
        $model = parent::update($attributes, $id);
        $this->skipPresenter($temporarySkipPresenter);

        if (Input::hasFile('img1')) {
            $model->img1 = Input::file('img1');
        } else {
            $model->img1 = null;
        }
        if (Input::hasFile('img2')) {
            $model->img2 = Input::file('img2');
        } else {
            $model->img2 = null;
        }
        if (Input::hasFile('img3')) {
            $model->img3 = Input::file('img3');
        } else {
            $model->img3 = null;
        }

        $model = $this->updateRelations($model, $attributes);
        $model->save();

        // create history
        $historyModel = $model->replicate();
        $historyModel->is_history = true;
        $historyModel->history_id = $model->id;
        $historyModel->save();

        return $this->parserResult($model);
    }
}
