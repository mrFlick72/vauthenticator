import {v4 as uuidv4} from 'uuid';

const randomClientApplicationIdGenerator = (): string => {
    return uuidv4();
}

export default randomClientApplicationIdGenerator
