<?php

namespace App\Http\Controllers\API;

use App\Http\Requests\API\CreateFoodAPIRequest;
use App\Http\Requests\API\UpdateFoodAPIRequest;
use App\Models\Food;
use App\Repositories\FoodRepository;
use Illuminate\Http\Request;
use App\Http\Controllers\AppBaseController;
use InfyOm\Generator\Criteria\LimitOffsetCriteria;
use Prettus\Repository\Criteria\RequestCriteria;
use Response;

/**
 * Class FoodController
 * @package App\Http\Controllers\API
 */

class FoodAPIController extends AppBaseController
{
    /** @var  FoodRepository */
    private $foodRepository;

    public function __construct(FoodRepository $foodRepo)
    {
        $this->foodRepository = $foodRepo;
    }

    /**
     * Display a listing of the Food which count > 0
     * GET|HEAD /foods
     *
     * @param Request $request
     * @return Response
     */
    public function index(Request $request)
    {
        $this->foodRepository->pushCriteria(new RequestCriteria($request));
        $this->foodRepository->pushCriteria(new LimitOffsetCriteria($request));
        $foods = $this->foodRepository
          ->all()
          ->where('is_history', false)
          ->where('count', '>', '0');

        return $this->sendResponse($foods->toArray(), 'Foods retrieved successfully');
    }

    /**
     * Display a listing of history of foods.
     * GET|HEAD /histories
     *
     * @param Request $request
     * @return Response
     */
    public function histories(Request $request)
    {
        $this->foodRepository->pushCriteria(new RequestCriteria($request));
        $this->foodRepository->pushCriteria(new LimitOffsetCriteria($request));
        $foods = $this->foodRepository->all()->where('is_history', true);

        return $this->sendResponse($foods->toArray(), 'Histories retrieved successfully');
    }

    /**
     * Store a newly created Food in storage.
     * POST /foods
     *
     * @param CreateFoodAPIRequest $request
     *
     * @return Response
     */
    public function store(CreateFoodAPIRequest $request)
    {
        $input = $request->all();

        $foods = $this->foodRepository->create($input);

        return $this->sendResponse($foods->toArray(), 'Food saved successfully');
    }

    /**
     * Display the specified Food.
     * GET|HEAD /foods/{id}
     *
     * @param  int $id
     *
     * @return Response
     */
    public function show($id)
    {
        /** @var Food $food */
        $food = $this->foodRepository->findWithoutFail($id);

        if (empty($food)) {
            return $this->sendError('Food not found');
        }

        return $this->sendResponse($food->toArray(), 'Food retrieved successfully');
    }

    /**
     * Update the specified Food in storage.
     * PUT/PATCH /foods/{id}
     *
     * @param  int $id
     * @param UpdateFoodAPIRequest $request
     *
     * @return Response
     */
    public function update($id, UpdateFoodAPIRequest $request)
    {
        $input = $request->all();

        /** @var Food $food */
        $food = $this->foodRepository->findWithoutFail($id);

        if (empty($food)) {
            return $this->sendError('Food not found');
        }

        $food = $this->foodRepository->update($input, $id);

        return $this->sendResponse($food->toArray(), 'Food updated successfully');
    }

    /**
     * Remove the specified Food from storage.
     * DELETE /foods/{id}
     *
     * @param  int $id
     *
     * @return Response
     */
    public function destroy($id)
    {
        /** @var Food $food */
        $food = $this->foodRepository->findWithoutFail($id);

        if (empty($food)) {
            return $this->sendError('Food not found');
        }

        $food->delete();

        return $this->sendResponse($id, 'Food deleted successfully');
    }
}
